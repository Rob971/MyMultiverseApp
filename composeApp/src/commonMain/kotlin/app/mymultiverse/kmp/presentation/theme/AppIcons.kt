package app.mymultiverse.kmp.presentation.theme

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {

    val Person: ImageVector
        get() {
            if (_person != null) return _person!!
            _person = ImageVector.Builder(
                name = "Filled.Person",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(12.0f, 12.0f)
                    curveTo(14.21f, 12.0f, 16.0f, 10.21f, 16.0f, 8.0f)
                    reflectiveCurveTo(14.21f, 4.0f, 12.0f, 4.0f)
                    reflectiveCurveTo(8.0f, 5.79f, 8.0f, 8.0f)
                    reflectiveCurveTo(9.79f, 12.0f, 12.0f, 12.0f)
                    close()
                    moveTo(12.0f, 14.0f)
                    curveTo(9.33f, 14.0f, 4.0f, 15.34f, 4.0f, 18.0f)
                    verticalLineTo(20.0f)
                    horizontalLineTo(20.0f)
                    verticalLineTo(18.0f)
                    curveTo(20.0f, 15.34f, 14.67f, 14.0f, 12.0f, 14.0f)
                    close()
                }
            }.build()
            return _person!!
        }
    private var _person: ImageVector? = null

    val ShoppingCart: ImageVector
        get() {
            if (_shoppingCart != null) return _shoppingCart!!
            _shoppingCart = ImageVector.Builder(
                name = "Filled.ShoppingCart",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(7.0f, 18.0f)
                    curveTo(5.9f, 18.0f, 5.01f, 18.9f, 5.01f, 20.0f)
                    reflectiveCurveTo(5.9f, 22.0f, 7.0f, 22.0f)
                    reflectiveCurveTo(9.0f, 21.1f, 9.0f, 20.0f)
                    reflectiveCurveTo(8.1f, 18.0f, 7.0f, 18.0f)
                    close()
                    moveTo(1.0f, 1.0f)
                    horizontalLineTo(3.0f)
                    lineTo(3.2f, 3.0f)
                    lineTo(19.0f, 3.0f)
                    curveTo(20.1f, 3.0f, 20.8f, 3.78f, 20.95f, 4.8f)
                    lineTo(22.3f, 14.8f)
                    curveTo(22.45f, 15.92f, 21.65f, 17.0f, 20.53f, 17.0f)
                    horizontalLineTo(5.5f)
                    lineTo(5.0f, 7.0f)
                    horizontalLineTo(21.0f)
                    lineTo(19.7f, 15.0f)
                    horizontalLineTo(7.0f)
                    curveTo(5.9f, 15.0f, 5.01f, 15.9f, 5.01f, 17.0f)
                    reflectiveCurveTo(5.9f, 19.0f, 7.0f, 19.0f)
                    horizontalLineTo(20.0f)
                    verticalLineTo(17.0f)
                    horizontalLineTo(7.82f)
                    lineTo(7.1f, 5.0f)
                    horizontalLineTo(2.0f)
                    lineTo(1.0f, 1.0f)
                    close()
                    moveTo(17.0f, 18.0f)
                    curveTo(15.9f, 18.0f, 15.01f, 18.9f, 15.01f, 20.0f)
                    reflectiveCurveTo(15.9f, 22.0f, 17.0f, 22.0f)
                    reflectiveCurveTo(19.0f, 21.1f, 19.0f, 20.0f)
                    reflectiveCurveTo(18.1f, 18.0f, 17.0f, 18.0f)
                    close()
                }
            }.build()
            return _shoppingCart!!
        }
    private var _shoppingCart: ImageVector? = null

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
                    reflectiveCurveTo(6.48f, 22.0f, 12.0f, 22.0f)
                    reflectiveCurveTo(22.0f, 17.52f, 22.0f, 12.0f)
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
                    reflectiveCurveTo(6.48f, 22.0f, 12.0f, 22.0f)
                    reflectiveCurveTo(22.0f, 17.52f, 22.0f, 12.0f)
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

    /**
     * Wallet — used for the "Budget" coming-soon feature.
     * Replaces the previous bank-building icon which implied institutional finance
     * rather than personal budget management.
     */
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
                // Wallet body
                path {
                    moveTo(21.0f, 7.0f)
                    horizontalLineTo(5.0f)
                    curveTo(4.45f, 7.0f, 4.0f, 6.55f, 4.0f, 6.0f)
                    reflectiveCurveTo(4.45f, 5.0f, 5.0f, 5.0f)
                    horizontalLineTo(21.0f)
                    verticalLineTo(3.0f)
                    horizontalLineTo(5.0f)
                    curveTo(3.35f, 3.0f, 2.0f, 4.35f, 2.0f, 6.0f)
                    verticalLineTo(18.0f)
                    curveTo(2.0f, 19.65f, 3.35f, 21.0f, 5.0f, 21.0f)
                    horizontalLineTo(21.0f)
                    curveTo(21.55f, 21.0f, 22.0f, 20.55f, 22.0f, 20.0f)
                    verticalLineTo(8.0f)
                    curveTo(22.0f, 7.45f, 21.55f, 7.0f, 21.0f, 7.0f)
                    close()
                }
                // Coin slot indicator inside the wallet
                path {
                    moveTo(18.0f, 15.5f)
                    curveTo(18.0f, 16.33f, 17.33f, 17.0f, 16.5f, 17.0f)
                    reflectiveCurveTo(15.0f, 16.33f, 15.0f, 15.5f)
                    reflectiveCurveTo(15.67f, 14.0f, 16.5f, 14.0f)
                    reflectiveCurveTo(18.0f, 14.67f, 18.0f, 15.5f)
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

    val Home: ImageVector
        get() {
            if (_home != null) return _home!!
            _home = ImageVector.Builder(
                name = "Filled.Home",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(10.0f, 20.0f)
                    verticalLineTo(14.0f)
                    horizontalLineTo(14.0f)
                    verticalLineTo(20.0f)
                    horizontalLineTo(19.0f)
                    verticalLineTo(12.0f)
                    horizontalLineTo(22.0f)
                    lineTo(12.0f, 3.0f)
                    lineTo(2.0f, 12.0f)
                    horizontalLineTo(5.0f)
                    verticalLineTo(20.0f)
                    horizontalLineTo(10.0f)
                    close()
                }
            }.build()
            return _home!!
        }
    private var _home: ImageVector? = null

    val Close: ImageVector
        get() {
            if (_close != null) return _close!!
            _close = ImageVector.Builder(
                name = "Filled.Close",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(19.0f, 6.41f)
                    lineTo(17.59f, 5.0f)
                    lineTo(12.0f, 10.59f)
                    lineTo(6.41f, 5.0f)
                    lineTo(5.0f, 6.41f)
                    lineTo(10.59f, 12.0f)
                    lineTo(5.0f, 17.59f)
                    lineTo(6.41f, 19.0f)
                    lineTo(12.0f, 13.41f)
                    lineTo(17.59f, 19.0f)
                    lineTo(19.0f, 17.59f)
                    lineTo(13.41f, 12.0f)
                    lineTo(19.0f, 6.41f)
                    close()
                }
            }.build()
            return _close!!
        }
    private var _close: ImageVector? = null

    val Lightbulb: ImageVector
        get() {
            if (_lightbulb != null) return _lightbulb!!
            _lightbulb = ImageVector.Builder(
                name = "Filled.Lightbulb",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(9.0f, 21.0f)
                    curveTo(9.0f, 21.55f, 9.45f, 22.0f, 10.0f, 22.0f)
                    horizontalLineTo(14.0f)
                    curveTo(14.55f, 22.0f, 15.0f, 21.55f, 15.0f, 21.0f)
                    verticalLineTo(20.0f)
                    horizontalLineTo(9.0f)
                    verticalLineTo(21.0f)
                    close()
                    moveTo(12.0f, 2.0f)
                    curveTo(8.13f, 2.0f, 5.0f, 5.13f, 5.0f, 9.0f)
                    curveTo(5.0f, 11.38f, 6.19f, 13.47f, 8.0f, 14.74f)
                    verticalLineTo(17.0f)
                    curveTo(8.0f, 17.55f, 8.45f, 18.0f, 9.0f, 18.0f)
                    horizontalLineTo(15.0f)
                    curveTo(15.55f, 18.0f, 16.0f, 17.55f, 16.0f, 17.0f)
                    verticalLineTo(14.74f)
                    curveTo(17.81f, 13.47f, 19.0f, 11.38f, 19.0f, 9.0f)
                    curveTo(19.0f, 5.13f, 15.87f, 2.0f, 12.0f, 2.0f)
                    close()
                }
            }.build()
            return _lightbulb!!
        }
    private var _lightbulb: ImageVector? = null

    val Language: ImageVector
        get() {
            if (_language != null) return _language!!
            _language = ImageVector.Builder(
                name = "Filled.Language",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(11.99f, 2.0f)
                    curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    reflectiveCurveTo(6.47f, 22.0f, 11.99f, 22.0f)
                    curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                    reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                    close()
                    moveTo(18.92f, 8.0f)
                    horizontalLineTo(15.97f)
                    curveTo(15.65f, 6.75f, 15.19f, 5.55f, 14.59f, 4.44f)
                    curveTo(16.43f, 5.07f, 17.96f, 6.34f, 18.92f, 8.0f)
                    close()
                    moveTo(12.0f, 4.04f)
                    curveTo(12.83f, 5.24f, 13.48f, 6.57f, 13.91f, 8.0f)
                    horizontalLineTo(10.09f)
                    curveTo(10.52f, 6.57f, 11.17f, 5.24f, 12.0f, 4.04f)
                    close()
                    moveTo(4.26f, 14.0f)
                    curveTo(4.1f, 13.36f, 4.0f, 12.69f, 4.0f, 12.0f)
                    reflectiveCurveTo(4.1f, 10.64f, 4.26f, 10.0f)
                    horizontalLineTo(7.64f)
                    curveTo(7.56f, 10.66f, 7.5f, 11.32f, 7.5f, 12.0f)
                    reflectiveCurveTo(7.56f, 13.34f, 7.64f, 14.0f)
                    horizontalLineTo(4.26f)
                    close()
                    moveTo(5.08f, 16.0f)
                    horizontalLineTo(8.03f)
                    curveTo(8.35f, 17.25f, 8.81f, 18.45f, 9.41f, 19.56f)
                    curveTo(7.57f, 18.93f, 6.04f, 17.66f, 5.08f, 16.0f)
                    close()
                    moveTo(8.03f, 8.0f)
                    horizontalLineTo(5.08f)
                    curveTo(6.04f, 6.34f, 7.57f, 5.07f, 9.41f, 4.44f)
                    curveTo(8.81f, 5.55f, 8.35f, 6.75f, 8.03f, 8.0f)
                    close()
                    moveTo(12.0f, 19.96f)
                    curveTo(11.17f, 18.76f, 10.52f, 17.43f, 10.09f, 16.0f)
                    horizontalLineTo(13.91f)
                    curveTo(13.48f, 17.43f, 12.83f, 18.76f, 12.0f, 19.96f)
                    close()
                    moveTo(14.34f, 14.0f)
                    horizontalLineTo(9.66f)
                    curveTo(9.57f, 13.34f, 9.5f, 12.68f, 9.5f, 12.0f)
                    reflectiveCurveTo(9.57f, 10.65f, 9.66f, 10.0f)
                    horizontalLineTo(14.34f)
                    curveTo(14.43f, 10.65f, 14.5f, 11.32f, 14.5f, 12.0f)
                    reflectiveCurveTo(14.43f, 13.34f, 14.34f, 14.0f)
                    close()
                    moveTo(14.59f, 19.56f)
                    curveTo(15.19f, 18.45f, 15.65f, 17.25f, 15.97f, 16.0f)
                    horizontalLineTo(18.92f)
                    curveTo(17.96f, 17.66f, 16.43f, 18.93f, 14.59f, 19.56f)
                    close()
                    moveTo(16.36f, 14.0f)
                    curveTo(16.44f, 13.34f, 16.5f, 12.68f, 16.5f, 12.0f)
                    reflectiveCurveTo(16.44f, 10.66f, 16.36f, 10.0f)
                    horizontalLineTo(19.74f)
                    curveTo(19.9f, 10.64f, 20.0f, 11.31f, 20.0f, 12.0f)
                    reflectiveCurveTo(19.9f, 13.36f, 19.74f, 14.0f)
                    horizontalLineTo(16.36f)
                    close()
                }
            }.build()
            return _language!!
        }
    private var _language: ImageVector? = null

    /**
     * Three horizontal bars — the universally-recognised drag/reorder handle.
     * Replaces the previous 6-dot grid which was ambiguous and low-contrast.
     */
    val DragHandle: ImageVector
        get() {
            if (_dragHandle != null) return _dragHandle!!
            _dragHandle = ImageVector.Builder(
                name = "Filled.DragHandle",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(3.0f, 8.0f)
                    horizontalLineTo(21.0f)
                    verticalLineTo(10.0f)
                    horizontalLineTo(3.0f)
                    close()
                    moveTo(3.0f, 12.0f)
                    horizontalLineTo(21.0f)
                    verticalLineTo(14.0f)
                    horizontalLineTo(3.0f)
                    close()
                    moveTo(3.0f, 16.0f)
                    horizontalLineTo(21.0f)
                    verticalLineTo(18.0f)
                    horizontalLineTo(3.0f)
                    close()
                }
            }.build()
            return _dragHandle!!
        }
    private var _dragHandle: ImageVector? = null

    /** Weekly meal plan — calendar with plate and utensils (distinct from generic date range). */
    val MealPlan: ImageVector
        get() {
            if (_mealPlan != null) return _mealPlan!!
            _mealPlan = ImageVector.Builder(
                name = "Filled.MealPlan",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(16.0f, 1.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineTo(8.0f)
                    verticalLineTo(1.0f)
                    horizontalLineTo(6.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineTo(5.0f)
                    curveToRelative(-1.11f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                    lineTo(3.0f, 19.0f)
                    curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(14.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(5.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    horizontalLineToRelative(-1.0f)
                    verticalLineTo(1.0f)
                    horizontalLineToRelative(-2.0f)
                    close()
                    moveTo(19.0f, 19.0f)
                    horizontalLineTo(5.0f)
                    verticalLineTo(8.0f)
                    horizontalLineToRelative(14.0f)
                    verticalLineToRelative(11.0f)
                    close()
                }
                // Fork — three tines + handle, positioned on the left half of the calendar interior.
                // Tine 1
                path {
                    moveTo(7.0f, 10.0f)
                    horizontalLineToRelative(1.0f)
                    verticalLineTo(13.0f)
                    horizontalLineToRelative(-1.0f)
                    close()
                }
                // Tine 2
                path {
                    moveTo(8.8f, 10.0f)
                    horizontalLineToRelative(1.0f)
                    verticalLineTo(13.0f)
                    horizontalLineToRelative(-1.0f)
                    close()
                }
                // Tine 3
                path {
                    moveTo(10.6f, 10.0f)
                    horizontalLineToRelative(1.0f)
                    verticalLineTo(13.0f)
                    horizontalLineToRelative(-1.0f)
                    close()
                }
                // Fork handle — bridges all three tines down to the grip
                path {
                    moveTo(8.1f, 13.0f)
                    horizontalLineToRelative(2.4f)
                    verticalLineTo(13.7f)
                    curveTo(10.5f, 14.0f, 9.8f, 14.3f, 9.3f, 14.3f)
                    curveTo(8.8f, 14.3f, 8.1f, 14.0f, 8.1f, 13.7f)
                    close()
                }
                path {
                    moveTo(8.7f, 14.3f)
                    horizontalLineToRelative(1.2f)
                    verticalLineTo(18.0f)
                    horizontalLineToRelative(-1.2f)
                    close()
                }
                // Knife — tapered blade + handle, positioned on the right half.
                // Blade (triangle): narrow at top, widening to knife-width
                path {
                    moveTo(14.0f, 10.0f)
                    lineTo(16.0f, 13.5f)
                    lineTo(14.0f, 13.5f)
                    close()
                }
                // Knife handle
                path {
                    moveTo(14.0f, 13.5f)
                    horizontalLineToRelative(1.5f)
                    verticalLineTo(18.0f)
                    horizontalLineToRelative(-1.5f)
                    close()
                }
            }.build()
            return _mealPlan!!
        }
    private var _mealPlan: ImageVector? = null

    /** Shared grocery checklist — clipboard with checked and pending rows. */
    val GroceryList: ImageVector
        get() {
            if (_groceryList != null) return _groceryList!!
            _groceryList = ImageVector.Builder(
                name = "Filled.GroceryList",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(8.0f, 2.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineTo(8.0f)
                    verticalLineTo(2.0f)
                    close()
                    moveTo(5.0f, 4.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    verticalLineToRelative(14.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(12.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(6.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    horizontalLineToRelative(-1.0f)
                    verticalLineTo(4.0f)
                    horizontalLineToRelative(-2.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineTo(8.0f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(6.0f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(5.0f)
                    close()
                }
                path {
                    moveTo(7.0f, 10.0f)
                    curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                    reflectiveCurveTo(9.0f, 10.55f, 9.0f, 10.0f)
                    reflectiveCurveTo(8.55f, 9.0f, 8.0f, 9.0f)
                    reflectiveCurveTo(7.0f, 9.45f, 7.0f, 10.0f)
                    close()
                    moveTo(10.0f, 9.5f)
                    horizontalLineToRelative(7.0f)
                    verticalLineToRelative(1.0f)
                    horizontalLineToRelative(-7.0f)
                    verticalLineTo(9.5f)
                    close()
                    moveTo(7.0f, 14.0f)
                    curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                    reflectiveCurveTo(9.0f, 14.55f, 9.0f, 14.0f)
                    reflectiveCurveTo(8.55f, 13.0f, 8.0f, 13.0f)
                    reflectiveCurveTo(7.0f, 13.45f, 7.0f, 14.0f)
                    close()
                    moveTo(10.0f, 13.5f)
                    horizontalLineToRelative(7.0f)
                    verticalLineToRelative(1.0f)
                    horizontalLineToRelative(-7.0f)
                    verticalLineTo(13.5f)
                    close()
                    moveTo(7.0f, 18.0f)
                    curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                    reflectiveCurveTo(9.0f, 18.55f, 9.0f, 18.0f)
                    reflectiveCurveTo(8.55f, 17.0f, 8.0f, 17.0f)
                    reflectiveCurveTo(7.0f, 17.45f, 7.0f, 18.0f)
                    close()
                    moveTo(10.0f, 17.5f)
                    horizontalLineToRelative(7.0f)
                    verticalLineToRelative(1.0f)
                    horizontalLineToRelative(-7.0f)
                    verticalLineTo(17.5f)
                    close()
                }
                // Visible check mark on the first row bullet (~4×4dp, previously 1.75×1.75dp).
                path {
                    moveTo(6.5f, 9.5f)
                    lineTo(7.75f, 10.75f)
                    lineTo(10.5f, 7.75f)
                    lineTo(9.75f, 7.0f)
                    lineTo(7.75f, 9.25f)
                    lineTo(7.25f, 8.75f)
                    close()
                }
            }.build()
            return _groceryList!!
        }
    private var _groceryList: ImageVector? = null

    /** Household / family collaboration — group of people. */
    val Household: ImageVector
        get() {
            if (_household != null) return _household!!
            _household = ImageVector.Builder(
                name = "Filled.Household",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(16.0f, 11.0f)
                    curveToRelative(1.66f, 0.0f, 2.99f, -1.34f, 2.99f, -3.0f)
                    reflectiveCurveTo(17.66f, 5.0f, 16.0f, 5.0f)
                    reflectiveCurveTo(13.0f, 6.34f, 13.0f, 8.0f)
                    reflectiveCurveTo(14.34f, 11.0f, 16.0f, 11.0f)
                    close()
                    moveTo(8.0f, 11.0f)
                    curveToRelative(1.66f, 0.0f, 2.99f, -1.34f, 2.99f, -3.0f)
                    reflectiveCurveTo(9.66f, 5.0f, 8.0f, 5.0f)
                    reflectiveCurveTo(5.0f, 6.34f, 5.0f, 8.0f)
                    reflectiveCurveTo(6.34f, 11.0f, 8.0f, 11.0f)
                    close()
                    moveTo(8.0f, 13.0f)
                    curveToRelative(-2.33f, 0.0f, -7.0f, 1.17f, -7.0f, 3.5f)
                    verticalLineTo(19.0f)
                    horizontalLineToRelative(14.0f)
                    verticalLineToRelative(-2.5f)
                    curveTo(15.0f, 14.17f, 10.33f, 13.0f, 8.0f, 13.0f)
                    close()
                    moveTo(16.0f, 13.0f)
                    curveToRelative(-0.29f, 0.0f, -0.62f, 0.02f, -0.97f, 0.05f)
                    curveToRelative(1.16f, 0.84f, 1.97f, 1.97f, 1.97f, 3.45f)
                    verticalLineTo(19.0f)
                    horizontalLineToRelative(6.0f)
                    verticalLineToRelative(-2.5f)
                    curveTo(23.0f, 14.34f, 18.33f, 13.0f, 16.0f, 13.0f)
                    close()
                }
            }.build()
            return _household!!
        }
    private var _household: ImageVector? = null

    /** Invite a member — person with add badge. */
    val PersonAdd: ImageVector
        get() {
            if (_personAdd != null) return _personAdd!!
            _personAdd = ImageVector.Builder(
                name = "Filled.PersonAdd",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path {
                    moveTo(15.0f, 12.0f)
                    curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                    reflectiveCurveTo(17.21f, 4.0f, 15.0f, 4.0f)
                    reflectiveCurveTo(11.0f, 5.79f, 11.0f, 8.0f)
                    reflectiveCurveTo(12.79f, 12.0f, 15.0f, 12.0f)
                    close()
                    moveTo(6.0f, 10.0f)
                    verticalLineTo(7.0f)
                    horizontalLineTo(4.0f)
                    verticalLineToRelative(3.0f)
                    horizontalLineTo(1.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineToRelative(3.0f)
                    verticalLineToRelative(3.0f)
                    horizontalLineToRelative(2.0f)
                    verticalLineToRelative(-3.0f)
                    horizontalLineToRelative(3.0f)
                    verticalLineToRelative(-2.0f)
                    horizontalLineTo(6.0f)
                    close()
                    moveTo(15.0f, 14.0f)
                    curveToRelative(-2.67f, 0.0f, -8.0f, 1.34f, -8.0f, 4.0f)
                    verticalLineTo(20.0f)
                    horizontalLineToRelative(16.0f)
                    verticalLineToRelative(-2.0f)
                    curveTo(23.0f, 15.34f, 17.67f, 14.0f, 15.0f, 14.0f)
                    close()
                }
            }.build()
            return _personAdd!!
        }
    private var _personAdd: ImageVector? = null
}
