package com.narcic.doh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.narcic.doh.core.AppCore
import com.narcic.doh.core.AppCore.Companion.coloredShadow
import com.narcic.doh.ui.MainViewModel
import com.narcic.doh.ui.theme.Cyan
import com.narcic.doh.ui.theme.DeepSpace
import com.narcic.doh.ui.theme.Mint
import com.narcic.doh.ui.theme.MintDeep
import com.narcic.doh.ui.theme.Mist
import com.narcic.doh.ui.theme.NarcicTheme
import com.narcic.doh.ui.theme.Roboto_Medium
import com.narcic.doh.ui.theme.Slate
import com.narcic.doh.ui.theme.Snow
import com.narcic.doh.ui.theme.StrokeNavy
import com.narcic.doh.ui.theme.StrokeNavySoft
import com.narcic.doh.ui.theme.SurfaceNavy
import com.narcic.doh.ui.theme.SurfaceNavyHigh
import com.narcic.doh.ui.theme.Ubuntu_Regular
import com.narcic.doh.ui.theme.Violet
import com.narcic.doh.ui.theme.VioletDeep

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val ctx = LocalContext.current
            val mainViewModel: MainViewModel = viewModel()

            LaunchedEffect(true) {
                mainViewModel.checkPageState(ctx)
            }

            NarcicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepSpace
                ) {
                    when (mainViewModel.pageState) {
                        0 -> Greeting()
                        1 -> Home()
                        2 -> Settings()
                    }
                }
            }
        }
    }
}

// ---------- shared ----------

@Composable
private fun GlowBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Cyan.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(size.width * 0.12f, size.height * 0.02f),
                radius = size.width * 1.05f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Violet.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(size.width * 0.92f, size.height * 0.80f),
                radius = size.width * 1.15f
            )
        )
    }
}

@Composable
private fun StatusPill(isOn: Boolean) {
    val accent by animateColorAsState(targetValue = if (isOn) Mint else Slate)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isOn) MintDeep else SurfaceNavy)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isOn) "Protected" else "Inactive",
            color = accent,
            fontSize = 12.sp,
            fontFamily = Roboto_Medium,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
private fun RoundIconButton(iconRes: Int, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceNavy)
            .border(1.dp, StrokeNavySoft, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = description,
            modifier = Modifier.size(20.dp),
            tint = Snow
        )
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceNavy)
            .border(1.dp, StrokeNavySoft, RoundedCornerShape(20.dp))
            .padding(18.dp),
        content = content
    )
}

// ---------- greeting ----------

@OptIn(ExperimentalTextApi::class)
@Composable
fun Greeting() {
    val ctx = LocalContext.current
    val mainViewModel: MainViewModel = viewModel()

    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(148.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = StrokeNavy,
                        radius = size.minDimension / 2,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(Cyan, Violet, Color.Transparent),
                            center = Offset(size.width / 2, size.height / 2)
                        ),
                        startAngle = -90f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.ic_narcic_logo),
                    contentDescription = Strings.greeting_image_des,
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = Strings.app_name,
                style = TextStyle(
                    brush = Brush.linearGradient(colors = listOf(Cyan, Violet))
                ),
                fontFamily = Ubuntu_Regular,
                fontSize = 30.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = Strings.greeting_text,
                color = Mist,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(36.dp))
            Button(
                onClick = { mainViewModel.greetingsDone(ctx) },
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .coloredShadow(
                        color = Cyan,
                        alpha = 0.35f,
                        borderRadius = 16.dp,
                        shadowRadius = 26.dp
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cyan,
                    contentColor = DeepSpace
                )
            ) {
                Text(
                    text = Strings.greeting_button_text,
                    fontFamily = Ubuntu_Regular,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

// ---------- home ----------

@Composable
private fun PowerButton(isOn: Boolean, onToggle: () -> Unit) {
    val transition = rememberInfiniteTransition()
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 6000, easing = LinearEasing))
    )
    val ringAlpha by animateFloatAsState(targetValue = if (isOn) 1f else 0f)

    Box(
        modifier = Modifier
            .size(230.dp)
            .coloredShadow(
                color = if (isOn) Cyan else VioletDeep,
                alpha = if (isOn) 0.45f else 0.12f,
                borderRadius = 115.dp,
                shadowRadius = 48.dp
            )
            .clip(CircleShape)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(if (isOn) angle else 0f)
        ) {
            val stroke = 3.dp.toPx()
            drawCircle(
                color = StrokeNavy,
                radius = size.minDimension / 2 - stroke / 2,
                style = Stroke(stroke)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        Cyan.copy(alpha = 0.9f * ringAlpha),
                        Violet.copy(alpha = ringAlpha),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2, size.height / 2)
                ),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Box(
            modifier = Modifier
                .size(158.dp)
                .clip(CircleShape)
                .border(1.dp, StrokeNavy, CircleShape)
                .background(
                    if (isOn) {
                        Brush.linearGradient(listOf(Cyan, Violet))
                    } else {
                        Brush.linearGradient(listOf(SurfaceNavyHigh, SurfaceNavyHigh))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_power),
                contentDescription = Strings.app_name,
                modifier = Modifier.size(54.dp),
                tint = if (isOn) DeepSpace else Mist
            )
        }
    }
}

@Composable
fun Home() {
    val ctx = LocalContext.current
    val mainViewModel: MainViewModel = viewModel()

    LaunchedEffect(true) {
        mainViewModel.checkVpnState(ctx)
    }

    var firstResume by remember { mutableStateOf(true) }
    AppCore.OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (!firstResume) {
                    mainViewModel.checkVpnState(ctx)
                } else {
                    firstResume = false
                }
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusPill(isOn = mainViewModel.vpnState)
            Spacer(modifier = Modifier.weight(1f))
            RoundIconButton(
                iconRes = R.drawable.ic_settings,
                description = Strings.home_setting_des,
                onClick = { mainViewModel.pageState = 2 }
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_narcic_logo),
                contentDescription = Strings.home_image_des,
                modifier = Modifier.size(46.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = Strings.app_name,
                color = Snow,
                fontFamily = Ubuntu_Regular,
                fontSize = 24.sp,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "DNS over HTTPS Guard",
                color = Slate,
                fontSize = 13.sp,
                fontFamily = Roboto_Medium
            )
            Spacer(modifier = Modifier.height(38.dp))
            PowerButton(
                isOn = mainViewModel.vpnState,
                onToggle = { mainViewModel.setVpnState(ctx) }
            )
            Spacer(modifier = Modifier.height(34.dp))
            Text(
                text = if (mainViewModel.vpnState) Strings.home_text_on else Strings.home_text_off,
                color = if (mainViewModel.vpnState) Snow else Slate,
                fontSize = 15.sp,
                fontFamily = Roboto_Medium,
                textAlign = TextAlign.Center
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp)
                .fillMaxWidth(0.86f)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceNavy)
                .border(1.dp, StrokeNavySoft, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceNavyHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dns),
                    contentDescription = Strings.setting_1,
                    modifier = Modifier.size(18.dp),
                    tint = Cyan
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = Strings.setting_1,
                    color = Mist,
                    fontSize = 12.sp,
                    fontFamily = Roboto_Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = Strings.setting_1_1,
                    color = Snow,
                    fontSize = 14.sp,
                    fontFamily = Ubuntu_Regular
                )
            }
        }
    }
}

// ---------- settings ----------

@Composable
fun Settings() {
    val mainViewModel: MainViewModel = viewModel()

    var logoState by remember { mutableStateOf(0) }

    BackHandler {
        mainViewModel.pageState = 1
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(
                    iconRes = R.drawable.ic_back,
                    description = Strings.setting_back,
                    onClick = { mainViewModel.pageState = 1 }
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Settings",
                    color = Snow,
                    fontFamily = Ubuntu_Regular,
                    fontSize = 18.sp
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                InfoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceNavyHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dns),
                                contentDescription = Strings.setting_1,
                                modifier = Modifier.size(18.dp),
                                tint = Cyan
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Strings.setting_1,
                                color = Mist,
                                fontSize = 12.sp,
                                fontFamily = Roboto_Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Encrypted DNS over HTTPS",
                                color = Snow,
                                fontSize = 14.sp,
                                fontFamily = Ubuntu_Regular
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Cyan.copy(alpha = 0.08f))
                                .border(1.dp, Cyan.copy(alpha = 0.4f), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = Strings.setting_1_1,
                                color = Cyan,
                                fontSize = 12.sp,
                                fontFamily = Roboto_Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                InfoCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_narcic_logo),
                            contentDescription = Strings.setting_logo,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .clickable { logoState++ }
                        )
                        if (logoState >= 12) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Image(
                                painter = painterResource(id = R.drawable.heart),
                                contentDescription = Strings.setting_logo,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = Strings.app_name,
                            color = Snow,
                            fontFamily = Ubuntu_Regular,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Strings.setting_credit,
                            color = Slate,
                            fontSize = 11.sp,
                            fontFamily = Roboto_Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(SurfaceNavyHigh)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "v${Strings.version}",
                                color = Mist,
                                fontSize = 12.sp,
                                fontFamily = Roboto_Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
