package com.jaime.ascend.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AirlineSeatIndividualSuite
import androidx.compose.material.icons.filled.AirlineSeatReclineExtra
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentPasteSearch
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Diversity1
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Fence
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hail
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NoMeals
import androidx.compose.material.icons.filled.NoiseAware
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PartyMode
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAddDisabled
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PhonelinkErase
import androidx.compose.material.icons.filled.PrivateConnectivity
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PsychologyAlt
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Quickreply
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SpeakerNotesOff
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps a string to an icon.
 * @param iconName The name of the icon.
 * @return The corresponding icon.
 * @author Jaime Martínez Fernández
 */
object IconMapper {
    fun getCategoryIcon(iconName: String?): ImageVector {
        if (iconName == null)
            return Icons.Filled.Category
        else
            return when (iconName.lowercase()) {
                "familyrestroom" -> Icons.Filled.FamilyRestroom
                "handshake" -> Icons.Filled.Handshake
                "school" -> Icons.Filled.School
                "favorite" -> Icons.Filled.Favorite
                "accountbalancewallet" -> Icons.Filled.AccountBalanceWallet
                "psychology" -> Icons.Filled.Psychology
                "fitnesscenter" -> Icons.Filled.FitnessCenter
                "selfimprovement" -> Icons.Filled.SelfImprovement
                else -> Icons.Filled.Category
            }
    }

    fun getHabitIcon(iconName: String?): ImageVector {
        if (iconName == null)
            return Icons.Filled.CheckCircle
        else
            return when (iconName.lowercase()) {
                //Social category
                "phoneaddiction" -> Icons.Filled.PhonelinkErase
                "socialcomparison" -> Icons.Filled.Compare
                "socialisolation" -> Icons.Filled.PrivateConnectivity
                "activelistening" -> Icons.Filled.RecordVoiceOver
                "appreciation" -> Icons.Filled.Diversity1
                "checkins" -> Icons.Filled.PhoneInTalk

                //Self-Care category
                "neglectinghygiene" -> Icons.Filled.Bathtub
                "norelaxation" -> Icons.Filled.Weekend
                "peoplepleasing" -> Icons.Filled.PersonAddDisabled
                "bedtimeroutine" -> Icons.Filled.Bedtime
                "metime" -> Icons.Filled.AccessTime
                "selfaffirmations" -> Icons.Filled.Favorite

                //Mental Health categoty
                "ignoringstress" -> Icons.Filled.SentimentVeryDissatisfied
                "negativeselftalk" -> Icons.Filled.Quickreply
                "overthinking" -> Icons.Filled.PsychologyAlt
                "digitaldetox" -> Icons.Filled.ScreenLockPortrait
                "gratitudejournal" -> Icons.Filled.CollectionsBookmark
                "healthyboundaries" -> Icons.Filled.HealthAndSafety

                //Physical Health category
                "excesscaffeine" -> Icons.Filled.Coffee
                "sedentary" -> Icons.Filled.AirlineSeatReclineExtra
                "skippingmeals" -> Icons.Filled.NoMeals
                "movementbreaks" -> Icons.Filled.DirectionsWalk
                "qualitysleep" -> Icons.Filled.AirlineSeatIndividualSuite
                "waterintake" -> Icons.Filled.WaterDrop

                //Career/Studies category
                "notesmaintenance" -> Icons.Filled.Notes
                "feedback" -> Icons.Filled.Feedback
                "budgetingmonthly" -> Icons.Filled.Wallet
                "deepwork" -> Icons.Filled.DesignServices
                "learnnewskill" -> Icons.Filled.NewReleases
                "morningplan" -> Icons.Filled.AlarmOn
                "quickreview" -> Icons.Filled.ContentPasteSearch
                "skilldrill" -> Icons.Filled.TrendingUp
                "trendanalysis" -> Icons.Filled.QueryStats
                "cramming" -> Icons.Filled.LocalLibrary
                "ineffectivemultitasking" -> Icons.Filled.Grading
                "procrastination" -> Icons.Filled.Bed
                "skippingclasses" -> Icons.Filled.SkipNext

                //Couple
                "romanticdate" -> Icons.Filled.WineBar
                "goalsanddreams" -> Icons.Filled.DarkMode
                "qualityconversation" -> Icons.Filled.Forum
                "smallgestures" -> Icons.Filled.VolunteerActivism
                "avoidingcommunication" -> Icons.Filled.MicOff
                "ignoringboundaries" -> Icons.Filled.Fence
                "keepingscore" -> Icons.Filled.Scoreboard
                "passiveaggresivecommunication" -> Icons.Filled.CrisisAlert
                "takingeachotherforgranted" -> Icons.Filled.SpeakerNotesOff

                //Finances
                "financialeducation" -> Icons.Filled.AutoStories
                "mindfulspending" -> Icons.Filled.CurrencyExchange
                "autosaving" -> Icons.Filled.Savings
                "expensetracking" -> Icons.Filled.BarChart
                "subscriptionreview" -> Icons.Filled.Subscriptions
                "impulsespending" -> Icons.Filled.Payments
                "latepayments" -> Icons.Filled.HourglassBottom
                "creditcardabuse" -> Icons.Filled.CreditCard

                //Family category
                "callfamilymember" -> Icons.Filled.Call
                "familymealtime" -> Icons.Filled.DinnerDining
                "morninghug" -> Icons.Filled.EmojiEmotions
                "dailycheckin" -> Icons.Filled.Chat
                "familysupporttime" -> Icons.Filled.BackHand
                "teamcleaning" -> Icons.Filled.CleaningServices
                "familywalk" -> Icons.Filled.DirectionsWalk
                "familyvisits" -> Icons.Filled.Group
                "familyshopping" -> Icons.Filled.ShoppingCart
                "dogwalking" -> Icons.Filled.Pets
                "familymovienight" -> Icons.Filled.Movie
                "comparingmembers" -> Icons.Filled.CompareArrows
                "conditionallove" -> Icons.Filled.Loyalty
                "poorcommunication" -> Icons.Filled.NoiseAware
                "unresolvedconflicts" -> Icons.Filled.Report
                else -> Icons.Filled.CheckCircle
            }
    }

    fun getMomentIcon(iconName: String?): ImageVector {
        if (iconName == null)
            return Icons.Filled.CheckCircle
        else
            return when (iconName.lowercase()) {
                "goparty" -> Icons.Filled.Nightlife
                "happypurchase" -> Icons.Filled.LocalMall
                "junkfood" -> Icons.Filled.Fastfood
                "localtourist"-> Icons.Filled.TravelExplore
                "sleepover" -> Icons.Filled.NightsStay
                "concerttickets" -> Icons.Filled.Speaker
                "fancydinner" -> Icons.Filled.DinnerDining
                "newbook" -> Icons.Filled.Book
                "spaday" -> Icons.Filled.Spa
                "weekendtrip" -> Icons.Filled.Hail
                else -> Icons.Filled.CheckCircle
            }

    }

}