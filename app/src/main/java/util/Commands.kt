package util

object Commands {

    enum class VoiceCommands(val rawValue:String){
        CLOSE("close"),
        EXIT("exit");

       /* companion object{
            fun parse(rawValue: String):VoiceCommands{
            }
        }*/
    }
    val quitRegex = Regex("(close|quit|back|finish|dismiss|close app)", RegexOption.IGNORE_CASE)
    val openCameraRegex = Regex("(open|open camera|launch camera|what is near me|describe|help)", RegexOption.IGNORE_CASE)
    val openChatBotRegex = Regex("(open chatbot|chatbot|hello|whats up|hey|chat)", RegexOption.IGNORE_CASE)
    val homeBotRegex = Regex("(home|main menu|front screen|go back)", RegexOption.IGNORE_CASE)
    val enrollBotRegex = Regex("(enroll|add faces|register users|recognize)", RegexOption.IGNORE_CASE)
//    val chatBotSummariseRegex = Regex("(Describe | summarise | wrap up | some | some rise | some raise )")
    val chatBotSummariseRegex = listOf("Describe","summarise","wrap up" , "some" , "some rise", "some raise", "some rice", "some race", "summary","diary", "summarize", "Open Diary")
    val captureScreenCommand = listOf("capture")

}