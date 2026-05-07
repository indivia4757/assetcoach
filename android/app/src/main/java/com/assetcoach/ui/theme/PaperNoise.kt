package com.assetcoach.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.random.Random

/**
 * 종이 질감 노이즈 오버레이.
 *
 * 디자인 시스템 §스페이싱 §종이 질감 오버레이 사양:
 *  - 단색 노이즈 픽셀 분포
 *  - opacity 0.18, mix-blend mode multiply 효과 (forest 색)
 *  - 256×256 타일을 캐싱해서 성능 영향 최소화
 *
 * 사용: `Modifier.paperNoise()` — 어떤 Composable 의 배경 위에든 덮어 쓸 수 있다.
 */
fun Modifier.paperNoise(
    color: Color = Color(0xFF1C2A1E),
    opacity: Float = 0.18f,
    seed: Int = 42
): Modifier = this.drawWithCache {
    val tile = generateNoiseTile(color, opacity, seed)
    onDrawWithContent {
        drawContent()
        drawNoiseTiled(tile)
    }
}

private fun generateNoiseTile(color: Color, opacity: Float, seed: Int): ImageBitmap {
    val size = 256
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint().apply {
        isAntiAlias = false
        this.color = android.graphics.Color.argb(
            (255 * opacity).toInt().coerceIn(0, 255),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
    }
    val rng = Random(seed)
    val dotCount = size * size / 8   // ≈ 12.5% pixel density
    repeat(dotCount) {
        val x = rng.nextFloat() * size
        val y = rng.nextFloat() * size
        canvas.drawPoint(x, y, paint)
    }
    return bmp.asImageBitmap()
}

private fun DrawScope.drawNoiseTiled(tile: ImageBitmap) {
    val tileSize = 256
    var y = 0
    while (y < size.height) {
        var x = 0
        while (x < size.width) {
            drawImage(
                image = tile,
                dstOffset = IntOffset(x, y),
                dstSize = IntSize(tileSize, tileSize)
            )
            x += tileSize
        }
        y += tileSize
    }
}
