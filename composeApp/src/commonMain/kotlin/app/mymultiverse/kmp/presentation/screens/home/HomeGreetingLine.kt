package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.runtime.Composable
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_greeting_afternoon
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_greeting_afternoon_personalized
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_greeting_evening
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_greeting_evening_personalized
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_greeting_morning
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_greeting_morning_personalized
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

internal fun currentLocalHour(): Int =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour

@Composable
internal fun homeGreetingSupportingLine(
    selection: HomeGreetingSelection,
): String =
    when (selection) {
        is HomeGreetingSelection.Personalized -> when (selection.dayPart) {
            HomeDayPart.Morning -> stringResource(
                Res.string.home_greeting_morning_personalized,
                selection.name,
            )
            HomeDayPart.Afternoon -> stringResource(
                Res.string.home_greeting_afternoon_personalized,
                selection.name,
            )
            HomeDayPart.Evening -> stringResource(
                Res.string.home_greeting_evening_personalized,
                selection.name,
            )
        }
        is HomeGreetingSelection.Generic -> when (selection.dayPart) {
            HomeDayPart.Morning -> stringResource(Res.string.home_greeting_morning)
            HomeDayPart.Afternoon -> stringResource(Res.string.home_greeting_afternoon)
            HomeDayPart.Evening -> stringResource(Res.string.home_greeting_evening)
        }
    }
