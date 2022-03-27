package bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatAction
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.logging.LogLevel
import data.remote.API_KEY
import data.remote.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val BOT_ANSWER_TIMEOUT = 30
private const val BOT_TOKEN = "5164778044:AAFvpAckgft-Fw4wT-GvMiPTK4e3IgX--gk"
private const val GIF_WAITING_URL = "http://favim.com/image/297045"

class WeatherBot(private val weatherRepository: WeatherRepository) {

    private var _chatId: ChatId? = null
    private val chatId by lazy { requireNotNull(_chatId) }
    private lateinit var country: String

    fun createBot(): Bot {
        return bot {
            timeout = BOT_ANSWER_TIMEOUT
            token = BOT_TOKEN
            logLevel = LogLevel.Error

            dispatch {
                setUpCommands()
                setUpCallbacks()
            }
        }
    }

    private fun Dispatcher.setUpCallbacks() {
        callbackQuery(callbackData = "getMyLocation") {
            bot.sendMessage(
                chatId = chatId,
                text = "Отправь мне свою локацию"
            )
            location {
                CoroutineScope(Dispatchers.IO).launch {
                    country = weatherRepository.getReversGeocodingCountryName(
                        location.latitude.toString(), location.longitude.toString(), "json"
                    ).address.country

                    val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                        listOf(
                            InlineKeyboardButton.CallbackData(
                                text = "Да, верно.",
                                callbackData = "yes_label"
                            )
                        )
                    )

                    bot.sendMessage(
                        chatId = chatId,
                        text = "Твой город - ${country}, верно? \n" +
                                " Если неверно, скинь локацию еще раз.",
                        replyMarkup = inlineKeyboardMarkup
                    )
                }
            }
        }

        callbackQuery(callbackData = "enterManually") {
            bot.sendMessage(chatId = chatId, text = "Хорошо, введи свой город.")
            message(Filter.Text) {
                country = message.text.toString()

                val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                    listOf(
                        InlineKeyboardButton.CallbackData(
                            text = "Да, верно.",
                            callbackData = "yes_label"
                        )
                    )
                )

                bot.sendMessage(
                    chatId = chatId,
                    text = "Твой город - ${country}, верно? \n" +
                            " Если неверно, введи свой город еще раз.",
                    replyMarkup = inlineKeyboardMarkup
                )
            }
        }

        callbackQuery(callbackData = "yes_label") {
            bot.apply {
                sendAnimation(chatId = chatId, animation = TelegramFile.ByUrl(GIF_WAITING_URL))
                sendMessage(chatId = chatId, text = "Узнаю твою погоду...")
                sendChatAction(chatId = chatId, action = ChatAction.TYPING)
            }
            CoroutineScope(Dispatchers.IO).launch {
                val currentWeather = weatherRepository.getCurrentWeather(
                    API_KEY,
                    country,
                    "no"
                )
                bot.sendMessage(
                    chatId = chatId,
                    text = """
                            ☁  Облачность: ${currentWeather.current.cloud}
                            🌡 Температура (градусы): ${currentWeather.current.temp_c}
                            🙎 ‍Ощущается как: ${currentWeather.current.feelslike_c}
                            💧 Влажность: ${currentWeather.current.humidity}
                            🌪 Направление ветра: ${currentWeather.current.wind_kph}
                            🧭 Давление: ${currentWeather.current.pressure_in}
                            🌓 Сейчас день? ${if (currentWeather.current.is_day == 1) "Да" else "Нет"}
                    """.trimIndent()
                )

                bot.sendMessage(
                    chatId = chatId,
                    text = "Если хочешь запросить еще раз погоду \n" +
                            " воспользуйся командой /weather"
                )
                country = ""
            }
        }
    }

    private fun Dispatcher.setUpCommands() {
        command("start") {
            _chatId = ChatId.fromId(message.chat.id)
            bot.sendMessage(
                chatId = chatId,
                text = "Привет! Я бот, умеющий отображать погоду! \n" +
                        "Для получения прогноза погоды введи команду /weather"
            )
        }

        command("weather") {
            val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Определить мой город (для мобильных устройств)",
                        callbackData = "getMyLocation"
                    )
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Ввести город в ручную",
                        callbackData = "enterManually"
                    )
                )
            )

            bot.sendMessage(
                chatId = chatId,
                text = "Для того, чтобы я смог отправить тебе погуду, \n" +
                        " мне нужно знать твой город.",
                replyMarkup = inlineKeyboardMarkup
            )
        }
    }
}