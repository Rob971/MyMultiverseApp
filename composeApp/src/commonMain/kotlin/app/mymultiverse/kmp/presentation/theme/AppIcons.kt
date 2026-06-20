package app.mymultiverse.kmp.presentation.theme

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {

    val Favorite: ImageVector
        get() = CheckCircle

    val Info: ImageVector
        get() = Sparkles

    val Person: ImageVector
        get() = CheckCircle

    val Refresh: ImageVector
        get() {
            if (_refresh != null) return _refresh!!
            _refresh = ImageVector.Builder(
                name = "Filled.Refresh",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(17.65f, 6.35f)
                    curveTo(16.2f, 4.9f, 14.21f, 4.0f, 12.0f, 4.0f)
                    curveToRelative(-4.42f, 0.0f, -7.99f, 3.58f, -7.99f, 8.0f)
                    reflectiveCurveToRelative(3.57f, 8.0f, 7.99f, 8.0f)
                    curveToRelative(3.73f, 0.0f, 6.84f, -2.55f, 7.73f, -6.0f)
                    horizontalLineToRelative(-2.08f)
                    curveToRelative(-0.82f, 2.33f, -3.04f, 4.0f, -5.65f, 4.0f)
                    curveToRelative(-3.31f, 0.0f, -6.0f, -2.69f, -6.0f, -6.0f)
                    reflectiveCurveToRelative(2.69f, -6.0f, 6.0f, -6.0f)
                    curveToRelative(1.66f, 0.0f, 3.14f, 0.69f, 4.22f, 1.78f)
                    lineTo(13.0f, 11.0f)
                    horizontalLineToRelative(7.0f)
                    verticalLineTo(4.0f)
                    lineToRelative(-2.35f, 2.35f)
                    close()
                }
            }.build()
            return _refresh!!
        }
    private var _refresh: ImageVector? = null

    val Star: ImageVector
        get() = Sparkles

    val ChevronLeft: ImageVector
        get() {
            if (_chevronLeft != null) {
                return _chevronLeft!!
            }
            _chevronLeft = ImageVector.Builder(
                name = "Filled.ChevronLeft",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(15.41f, 7.41f)
                    lineTo(14.0f, 6.0f)
                    lineTo(8.0f, 12.0f)
                    lineTo(14.0f, 18.0f)
                    lineTo(15.41f, 16.59f)
                    lineTo(10.83f, 12.0f)
                    close()
                }
            }.build()
            return _chevronLeft!!
        }
    private var _chevronLeft: ImageVector? = null

    val ChevronRight: ImageVector
        get() {
            if (_chevronRight != null) {
                return _chevronRight!!
            }
            _chevronRight = ImageVector.Builder(
                name = "Filled.ChevronRight",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(10.0f, 6.0f)
                    lineTo(8.59f, 7.41f)
                    lineTo(13.17f, 12.0f)
                    lineTo(8.59f, 16.59f)
                    lineTo(10.0f, 18.0f)
                    lineTo(16.0f, 12.0f)
                    close()
                }
            }.build()
            return _chevronRight!!
        }
    private var _chevronRight: ImageVector? = null

    val ArrowBack: ImageVector
        get() {
            if (_arrowBack != null) {
                return _arrowBack!!
            }
            _arrowBack = ImageVector.Builder(
                name = "AutoMirrored.Filled.ArrowBack",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
                autoMirror = true,
            ).apply {
                path {
                    moveTo(20.0f, 11.0f)
                    horizontalLineTo(7.83f)
                    lineToRelative(5.59f, -5.59f)
                    lineTo(12.0f, 4.0f)
                    lineTo(4.0f, 12.0f)
                    lineTo(12.0f, 20.0f)
                    lineToRelative(1.41f, -1.41f)
                    lineTo(7.83f, 13.0f)
                    horizontalLineTo(20.0f)
                    verticalLineToRelative(-2.0f)
                    close()
                }
            }.build()
            return _arrowBack!!
        }
    private var _arrowBack: ImageVector? = null

    val Add: ImageVector
        get() {
            if (_add != null) {
                return _add!!
            }
            _add = ImageVector.Builder(
                name = "Filled.Add",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(19.0f, 13.0f)
                    horizontalLineToRelative(-6.0f)
                    verticalLineToRelative(6.0f)
                    horizontalLineToRelative(-2.0f)
                    verticalLineToRelative(-6.0f)
                    horizontalLineTo(5.0f)
                    verticalLineToRelative(-2.0f)
                    horizontalLineToRelative(6.0f)
                    verticalLineTo(5.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineToRelative(6.0f)
                    horizontalLineToRelative(6.0f)
                    verticalLineToRelative(2.0f)
                    close()
                }
            }.build()
            return _add!!
        }
    private var _add: ImageVector? = null
        
    val Delete: ImageVector
        get() {
            if (_delete != null) {
                return _delete!!
            }
            _delete = ImageVector.Builder(
                name = "Filled.Delete",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(6.0f, 19.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(8.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(7.0f)
                    horizontalLineTo(6.0f)
                    verticalLineToRelative(12.0f)
                    close()
                    moveTo(19.0f, 4.0f)
                    horizontalLineToRelative(-3.5f)
                    lineToRelative(-1.0f, -1.0f)
                    horizontalLineToRelative(-5.0f)
                    lineToRelative(-1.0f, 1.0f)
                    horizontalLineTo(5.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineToRelative(14.0f)
                    verticalLineTo(4.0f)
                    close()
                }
            }.build()
            return _delete!!
        }
    private var _delete: ImageVector? = null

    val Edit: ImageVector
        get() {
            if (_edit != null) {
                return _edit!!
            }
            _edit = ImageVector.Builder(
                name = "Filled.Edit",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(3.0f, 17.25f)
                    verticalLineTo(21.0f)
                    horizontalLineToRelative(3.75f)
                    lineTo(17.81f, 9.94f)
                    lineTo(14.06f, 6.19f)
                    lineTo(3.0f, 17.25f)
                    close()
                    moveTo(20.71f, 7.04f)
                    curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                    lineToRelative(-2.34f, -2.34f)
                    curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                    lineToRelative(-1.83f, 1.83f)
                    lineToRelative(3.75f, 3.75f)
                    lineTo(20.71f, 7.04f)
                    close()
                }
            }.build()
            return _edit!!
        }
    private var _edit: ImageVector? = null

    val CheckCircle: ImageVector
        get() {
            if (_checkCircle != null) {
                return _checkCircle!!
            }
            _checkCircle = ImageVector.Builder(
                name = "Filled.CheckCircle",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(12.0f, 2.0f)
                    curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    reflectiveCurveTo(4.48f, 10.0f, 10.0f, 10.0f)
                    reflectiveCurveTo(10.0f, -4.48f, 10.0f, -10.0f)
                    reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                    close()
                    moveTo(10.0f, 17.0f)
                    lineToRelative(-5.0f, -5.0f)
                    lineToRelative(1.41f, -1.41f)
                    lineTo(10.0f, 14.17f)
                    lineToRelative(7.59f, -7.59f)
                    lineTo(19.0f, 8.0f)
                    lineToRelative(-9.0f, 9.0f)
                    close()
                }
            }.build()
            return _checkCircle!!
        }
    private var _checkCircle: ImageVector? = null

    val RadioButtonUnchecked: ImageVector
        get() {
            if (_radioButtonUnchecked != null) {
                return _radioButtonUnchecked!!
            }
            _radioButtonUnchecked = ImageVector.Builder(
                name = "Filled.RadioButtonUnchecked",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(12.0f, 2.0f)
                    curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    reflectiveCurveTo(4.48f, 10.0f, 10.0f, 10.0f)
                    reflectiveCurveTo(10.0f, -4.48f, 10.0f, -10.0f)
                    reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                    close()
                    moveTo(12.0f, 20.0f)
                    curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                    reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                    reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                    reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                    close()
                }
            }.build()
            return _radioButtonUnchecked!!
        }
    private var _radioButtonUnchecked: ImageVector? = null

    val Notifications: ImageVector
        get() {
            if (_notifications != null) {
                return _notifications!!
            }
            _notifications = ImageVector.Builder(
                name = "Filled.Notifications",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(12.0f, 22.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    horizontalLineToRelative(-4.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    close()
                    moveTo(18.0f, 16.0f)
                    verticalLineToRelative(-5.0f)
                    curveToRelative(0.0f, -3.07f, -1.63f, -5.64f, -4.5f, -6.32f)
                    verticalLineTo(4.0f)
                    curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
                    reflectiveCurveTo(10.5f, 3.17f, 10.5f, 4.0f)
                    verticalLineToRelative(0.68f)
                    curveTo(7.64f, 5.36f, 6.0f, 7.92f, 6.0f, 11.0f)
                    verticalLineToRelative(5.0f)
                    lineToRelative(-2.0f, 2.0f)
                    verticalLineToRelative(1.0f)
                    horizontalLineToRelative(16.0f)
                    verticalLineToRelative(-1.0f)
                    lineToRelative(-2.0f, -2.0f)
                    close()
                }
            }.build()
            return _notifications!!
        }
    private var _notifications: ImageVector? = null

    val Sparkles: ImageVector
        get() {
            if (_sparkles != null) {
                return _sparkles!!
            }
            _sparkles = ImageVector.Builder(
                name = "Filled.Sparkles",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(19.0f, 9.0f)
                    lineTo(20.25f, 6.25f)
                    lineTo(23.0f, 5.0f)
                    lineTo(20.25f, 3.75f)
                    lineTo(19.0f, 1.0f)
                    lineTo(17.75f, 3.75f)
                    lineTo(15.0f, 5.0f)
                    lineTo(17.75f, 6.25f)
                    lineTo(19.0f, 9.0f)
                    close()
                    moveTo(11.5f, 9.5f)
                    lineTo(13.0f, 6.0f)
                    lineTo(14.5f, 9.5f)
                    lineTo(18.0f, 11.0f)
                    lineTo(14.5f, 12.5f)
                    lineTo(13.0f, 16.0f)
                    lineTo(11.5f, 12.5f)
                    lineTo(8.0f, 11.0f)
                    lineTo(11.5f, 9.5f)
                    close()
                    moveTo(19.0f, 15.0f)
                    lineTo(17.75f, 17.75f)
                    lineTo(15.0f, 19.0f)
                    lineTo(17.75f, 20.25f)
                    lineTo(19.0f, 23.0f)
                    lineTo(20.25f, 20.25f)
                    lineTo(23.0f, 19.0f)
                    lineTo(20.25f, 17.75f)
                    lineTo(19.0f, 15.0f)
                    close()
                }
            }.build()
            return _sparkles!!
        }
    private var _sparkles: ImageVector? = null

    val Restaurant: ImageVector
        get() {
            if (_restaurant != null) return _restaurant!!
            _restaurant = ImageVector.Builder(
                name = "Filled.Restaurant",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(11.0f, 9.0f)
                    horizontalLineTo(9.0f)
                    verticalLineTo(2.0f)
                    horizontalLineTo(7.0f)
                    verticalLineTo(9.0f)
                    horizontalLineTo(5.0f)
                    verticalLineTo(2.0f)
                    horizontalLineTo(3.0f)
                    verticalLineTo(9.0f)
                    curveTo(3.0f, 11.12f, 4.66f, 12.84f, 6.75f, 12.97f)
                    verticalLineTo(22.0f)
                    horizontalLineTo(9.25f)
                    verticalLineTo(12.97f)
                    curveTo(11.34f, 12.84f, 13.0f, 11.12f, 13.0f, 9.0f)
                    verticalLineTo(5.0f)
                    horizontalLineTo(11.0f)
                    verticalLineTo(9.0f)
                    close()
                    moveTo(21.0f, 9.0f)
                    verticalLineTo(2.0f)
                    horizontalLineTo(19.0f)
                    verticalLineTo(9.0f)
                    curveTo(19.0f, 10.1f, 18.1f, 11.0f, 17.0f, 11.0f)
                    reflectiveCurveTo(15.0f, 10.1f, 15.0f, 9.0f)
                    verticalLineTo(2.0f)
                    horizontalLineTo(13.0f)
                    verticalLineTo(9.0f)
                    curveTo(13.0f, 11.12f, 14.66f, 12.84f, 16.75f, 12.97f)
                    verticalLineTo(22.0f)
                    horizontalLineTo(19.25f)
                    verticalLineTo(12.97f)
                    curveTo(21.34f, 12.84f, 23.0f, 11.12f, 23.0f, 9.0f)
                    verticalLineTo(2.0f)
                    horizontalLineTo(21.0f)
                    verticalLineTo(9.0f)
                    close()
                }
            }.build()
            return _restaurant!!
        }
    private var _restaurant: ImageVector? = null

    val Explore: ImageVector
        get() {
            if (_explore != null) return _explore!!
            _explore = ImageVector.Builder(
                name = "Filled.Explore",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(12.0f, 2.0f)
                    curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    reflectiveCurveTo(6.48f, 22.0f, 12.0f, 22.0f)
                    reflectiveCurveTo(22.0f, 17.52f, 22.0f, 12.0f)
                    reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                    close()
                    moveTo(12.0f, 20.0f)
                    curveTo(7.59f, 20.0f, 4.0f, 16.41f, 4.0f, 12.0f)
                    reflectiveCurveTo(7.59f, 4.0f, 12.0f, 4.0f)
                    reflectiveCurveTo(20.0f, 7.59f, 20.0f, 12.0f)
                    reflectiveCurveTo(16.41f, 20.0f, 12.0f, 20.0f)
                    close()
                    moveTo(12.0f, 6.0f)
                    curveTo(9.79f, 6.0f, 8.0f, 7.79f, 8.0f, 10.0f)
                    curveTo(8.0f, 13.0f, 12.0f, 18.0f, 12.0f, 18.0f)
                    reflectiveCurveTo(16.0f, 13.0f, 16.0f, 10.0f)
                    curveTo(16.0f, 7.79f, 14.21f, 6.0f, 12.0f, 6.0f)
                    close()
                    moveTo(12.0f, 11.5f)
                    curveTo(11.17f, 11.5f, 10.5f, 10.83f, 10.5f, 10.0f)
                    reflectiveCurveTo(11.17f, 8.5f, 12.0f, 8.5f)
                    reflectiveCurveTo(13.5f, 9.17f, 13.5f, 10.0f)
                    reflectiveCurveTo(12.83f, 11.5f, 12.0f, 11.5f)
                    close()
                }
            }.build()
            return _explore!!
        }
    private var _explore: ImageVector? = null

    val AccountBalance: ImageVector
        get() {
            if (_accountBalance != null) return _accountBalance!!
            _accountBalance = ImageVector.Builder(
                name = "Filled.AccountBalance",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(4.0f, 10.0f)
                    verticalLineTo(20.0f)
                    horizontalLineTo(8.0f)
                    verticalLineTo(10.0f)
                    horizontalLineTo(4.0f)
                    close()
                    moveTo(10.0f, 10.0f)
                    verticalLineTo(20.0f)
                    horizontalLineTo(14.0f)
                    verticalLineTo(10.0f)
                    horizontalLineTo(10.0f)
                    close()
                    moveTo(16.0f, 10.0f)
                    verticalLineTo(20.0f)
                    horizontalLineTo(20.0f)
                    verticalLineTo(10.0f)
                    horizontalLineTo(16.0f)
                    close()
                    moveTo(2.0f, 22.0f)
                    horizontalLineTo(22.0f)
                    verticalLineTo(24.0f)
                    horizontalLineTo(2.0f)
                    verticalLineTo(22.0f)
                    close()
                    moveTo(12.0f, 7.0f)
                    lineTo(22.0f, 2.0f)
                    lineTo(2.0f, 2.0f)
                    lineTo(12.0f, 7.0f)
                    close()
                }
            }.build()
            return _accountBalance!!
        }
    private var _accountBalance: ImageVector? = null

    val Check: ImageVector
        get() {
            if (_check != null) return _check!!
            _check = ImageVector.Builder(
                name = "Filled.Check",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(9.0f, 16.17f)
                    lineTo(4.83f, 12.0f)
                    lineTo(3.41f, 13.41f)
                    lineTo(9.0f, 19.0f)
                    lineTo(21.0f, 7.0f)
                    lineTo(19.59f, 5.59f)
                    lineTo(9.0f, 16.17f)
                    close()
                }
            }.build()
            return _check!!
        }
    private var _check: ImageVector? = null

    val MoreVert: ImageVector
        get() {
            if (_moreVert != null) return _moreVert!!
            _moreVert = ImageVector.Builder(
                name = "Filled.MoreVert",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(12.0f, 8.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                    reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                    reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                    close()
                    moveTo(12.0f, 10.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                    reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                    reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                    moveTo(12.0f, 16.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                    reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                    reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                }
            }.build()
            return _moreVert!!
        }
    private var _moreVert: ImageVector? = null

    val DateRange: ImageVector
        get() {
            if (_dateRange != null) return _dateRange!!
            _dateRange = ImageVector.Builder(
                name = "Filled.DateRange",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(9.0f, 11.0f)
                    horizontalLineTo(7.0f)
                    verticalLineTo(9.0f)
                    horizontalLineTo(9.0f)
                    verticalLineTo(11.0f)
                    close()
                    moveTo(13.0f, 11.0f)
                    horizontalLineTo(11.0f)
                    verticalLineTo(9.0f)
                    horizontalLineTo(13.0f)
                    verticalLineTo(11.0f)
                    close()
                    moveTo(17.0f, 11.0f)
                    horizontalLineTo(15.0f)
                    verticalLineTo(9.0f)
                    horizontalLineTo(17.0f)
                    verticalLineTo(11.0f)
                    close()
                    moveTo(19.0f, 4.0f)
                    horizontalLineTo(18.0f)
                    verticalLineTo(2.0f)
                    horizontalLineTo(16.0f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(8.0f)
                    verticalLineTo(2.0f)
                    horizontalLineTo(6.0f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(5.0f)
                    curveToRelative(-1.11f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                    lineTo(3.0f, 20.0f)
                    curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                    horizontalLineTo(19.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(6.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                    moveTo(19.0f, 20.0f)
                    horizontalLineTo(5.0f)
                    verticalLineTo(9.0f)
                    horizontalLineTo(19.0f)
                    verticalLineTo(20.0f)
                    close()
                }
            }.build()
            return _dateRange!!
        }
    private var _dateRange: ImageVector? = null

    val KeyboardArrowDown: ImageVector
        get() {
            if (_keyboardArrowDown != null) return _keyboardArrowDown!!
            _keyboardArrowDown = ImageVector.Builder(
                name = "Filled.KeyboardArrowDown",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(7.41f, 8.59f)
                    lineTo(12.0f, 13.17f)
                    lineTo(16.59f, 8.59f)
                    lineTo(18.0f, 10.0f)
                    lineTo(12.0f, 16.0f)
                    lineTo(6.0f, 10.0f)
                    lineTo(7.41f, 8.59f)
                    close()
                }
            }.build()
            return _keyboardArrowDown!!
        }
    private var _keyboardArrowDown: ImageVector? = null

    val KeyboardArrowUp: ImageVector
        get() {
            if (_keyboardArrowUp != null) return _keyboardArrowUp!!
            _keyboardArrowUp = ImageVector.Builder(
                name = "Filled.KeyboardArrowUp",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(7.41f, 15.41f)
                    lineTo(12.0f, 10.83f)
                    lineTo(16.59f, 15.41f)
                    lineTo(18.0f, 14.0f)
                    lineTo(12.0f, 8.0f)
                    lineTo(6.0f, 14.0f)
                    lineTo(7.41f, 15.41f)
                    close()
                }
            }.build()
            return _keyboardArrowUp!!
        }
    private var _keyboardArrowUp: ImageVector? = null
}
