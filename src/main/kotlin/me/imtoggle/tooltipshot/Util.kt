package me.imtoggle.tooltipshot

import cc.polyfrost.oneconfig.utils.IOUtils
import cc.polyfrost.oneconfig.utils.dsl.mc
import me.imtoggle.tooltipshot.mixin.GuiContainerAccessor
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.shader.Framebuffer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.client.config.GuiUtils
import org.apache.commons.lang3.SystemUtils
import org.lwjgl.BufferUtils
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import sun.awt.datatransfer.DataTransferer
import sun.awt.datatransfer.SunClipboard
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun screenshot() {
    mc.currentScreen ?: return
    if (mc.currentScreen !is GuiContainer) return
    if (ModConfig.shotKeyBind.keyBinds.isEmpty()) return
    if (Keyboard.getEventKey() != ModConfig.shotKeyBind.keyBinds[0]) return
    val theSlot = (mc.currentScreen as GuiContainerAccessor).theSlot
    if ((mc.thePlayer.inventory.itemStack == null) && theSlot != null && theSlot.hasStack) {
        renderToolTip(theSlot.stack)
    }
}

fun renderToolTip(stack: ItemStack) {
    val list = stack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips)
    for (i in list.indices) {
        if (i == 0) {
            list[i] = stack.rarity.rarityColor.toString() + list[i] as String
        } else {
            list[i] = EnumChatFormatting.GRAY.toString() + list[i] as String
        }
    }
    drawHoveringText(list)
}

fun drawHoveringText(textLines: List<String?>) {
    if (textLines.isNotEmpty()) {
        var i = 0

        for (s in textLines) {
            val j = mc.fontRendererObj.getStringWidth(s)
            if (j > i) {
                i = j
            }
        }

        val start = 4
        var k = 8
        if (textLines.size > 1) {
            k += 2 + (textLines.size - 1) * 10
        }
        val l = -267386864
        val width = (i + 8) * ModConfig.screenshotScale
        val height = (k + 8) * ModConfig.screenshotScale
        val framebuffer = Framebuffer(width, height, false)
        framebuffer.framebufferClear()
        GlStateManager.matrixMode(5889)
        GlStateManager.loadIdentity()
        GlStateManager.ortho(0.0, width.toDouble(), height.toDouble(), 0.0, 1000.0, 3000.0)
        GlStateManager.matrixMode(5888)
        GlStateManager.loadIdentity()
        GlStateManager.translate(0.0f, 0.0f, -2000.0f)
        framebuffer.bindFramebuffer(true)
        GlStateManager.disableRescaleNormal()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.scale(ModConfig.screenshotScale.toFloat(), ModConfig.screenshotScale.toFloat(), 1f)
        GuiUtils.drawGradientRect(0, start - 3, start - 4, start + i + 3, start - 3, l, l)
        GuiUtils.drawGradientRect(0, start - 3, start + k + 3, start + i + 3, start + k + 4, l, l)
        GuiUtils.drawGradientRect(0, start - 3, start - 3, start + i + 3, start + k + 3, l, l)
        GuiUtils.drawGradientRect(0, start - 4, start - 3, start - 3, start + k + 3, l, l)
        GuiUtils.drawGradientRect(0, start + i + 3, start - 3, start + i + 4, start + k + 3, l, l)
        val i1 = 1347420415
        val j1 = (i1 and 16711422) shr 1 or (i1 and -16777216)
        GuiUtils.drawGradientRect(0, start - 3, start - 3 + 1, start - 3 + 1, start + k + 3 - 1, i1, j1)
        GuiUtils.drawGradientRect(0, start + i + 2, start - 3 + 1, start + i + 3, start + k + 3 - 1, i1, j1)
        GuiUtils.drawGradientRect(0, start - 3, start - 3, start + i + 3, start - 3 + 1, i1, i1)
        GuiUtils.drawGradientRect(0, start - 3, start + k + 2, start + i + 3, start + k + 3, j1, j1)
        var textY = 4
        for (k1 in textLines.indices) {
            mc.fontRendererObj.drawStringWithShadow(textLines[k1], 4.toFloat(), textY.toFloat(), -1)
            if (k1 == 0) {
                textY += 2
            }
            textY += 10
        }
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
        RenderHelper.enableStandardItemLighting()
        GlStateManager.enableRescaleNormal()
        framebuffer.screenshot().copyToClipboard()
        mc.entityRenderer.setupOverlayRendering()
        mc.framebuffer.bindFramebuffer(true)
    }
}

/**
 * from https://github.com/Polyfrost/Chatting
 */
fun BufferedImage.copyToClipboard() {
    if (SystemUtils.IS_OS_WINDOWS) {
        try {
            val width = this.width
            val height = this.height
            val hdrSize = 0x28
            val buffer: ByteBuffer = ByteBuffer.allocate(hdrSize + width * height * 4)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            //Header size
            buffer.putInt(hdrSize)
            //Width
            buffer.putInt(width)
            //Int32 biHeight;
            buffer.putInt(height)
            //Int16 biPlanes;
            buffer.put(1.toByte())
            buffer.put(0.toByte())
            //Int16 biBitCount;
            buffer.put(32.toByte())
            buffer.put(0.toByte())
            //Compression
            buffer.putInt(0)
            //Int32 biSizeImage;
            buffer.putInt(width * height * 4)
            buffer.putInt(0)
            buffer.putInt(0)
            buffer.putInt(0)
            buffer.putInt(0)

            //Image data
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val argb: Int = this.getRGB(x, height - y - 1)
                    if (argb shr 24 and 0xFF == 0) {
                        buffer.putInt(0x00000000)
                    } else {
                        buffer.putInt(argb)
                    }
                }
            }
            buffer.flip()
            val hdrSizev5 = 0x7C
            val bufferv5: ByteBuffer = ByteBuffer.allocate(hdrSizev5 + width * height * 4)
            bufferv5.order(ByteOrder.LITTLE_ENDIAN)
            //Header size
            bufferv5.putInt(hdrSizev5)
            //Width
            bufferv5.putInt(width)
            //Int32 biHeight;
            bufferv5.putInt(height)
            //Int16 biPlanes;
            bufferv5.put(1.toByte())
            bufferv5.put(0.toByte())
            //Int16 biBitCount;
            bufferv5.put(32.toByte())
            bufferv5.put(0.toByte())
            //Compression
            bufferv5.putInt(0)
            //Int32 biSizeImage;
            bufferv5.putInt(width * height * 4)
            bufferv5.putInt(0)
            bufferv5.putInt(0)
            bufferv5.putInt(0)
            bufferv5.putInt(0)
            bufferv5.order(ByteOrder.BIG_ENDIAN)
            bufferv5.putInt(-0x1000000)
            bufferv5.putInt(0x00FF0000)
            bufferv5.putInt(0x0000FF00)
            bufferv5.putInt(0x000000FF)
            bufferv5.order(ByteOrder.LITTLE_ENDIAN)

            //BGRs
            bufferv5.put(0x42.toByte())
            bufferv5.put(0x47.toByte())
            bufferv5.put(0x52.toByte())
            bufferv5.put(0x73.toByte())
            for (i in bufferv5.position() until hdrSizev5) {
                bufferv5.put(0.toByte())
            }

            //Image data
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val argb: Int = this.getRGB(x, height - y - 1)
                    val a = argb shr 24 and 0xFF
                    var r = argb shr 16 and 0xFF
                    var g = argb shr 8 and 0xFF
                    var b = argb and 0xFF
                    r = r * a / 0xFF
                    g = g * a / 0xFF
                    b = b * a / 0xFF
                    bufferv5.putInt(a shl 24 or (r shl 16) or (g shl 8) or b)
                }
            }
            bufferv5.flip()
            val clip = Toolkit.getDefaultToolkit().systemClipboard
            val dt = DataTransferer.getInstance()
            val f: Field = dt.javaClass.getDeclaredField("CF_DIB")
            f.isAccessible = true
            val format: Long = f.getLong(null)
            val openClipboard: Method = clip.javaClass.getDeclaredMethod("openClipboard", SunClipboard::class.java)
            openClipboard.isAccessible = true
            openClipboard.invoke(clip, clip)
            val publishClipboardData: Method = clip.javaClass.getDeclaredMethod(
                "publishClipboardData",
                Long::class.javaPrimitiveType,
                ByteArray::class.java
            )
            publishClipboardData.isAccessible = true
            val arr: ByteArray = buffer.array()
            publishClipboardData.invoke(clip, format, arr)
            val closeClipboard: Method = clip.javaClass.getDeclaredMethod("closeClipboard")
            closeClipboard.isAccessible = true
            closeClipboard.invoke(clip)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    val pixels: IntArray =
        this.getRGB(0, 0, this.width, this.height, null, 0, this.width)
    val newImage = BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB)
    newImage.setRGB(0, 0, newImage.width, newImage.height, pixels, 0, newImage.width)

    try {
        IOUtils.copyImageToClipboard(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * from https://github.com/Moulberry/HyChat
 */
fun Framebuffer.screenshot(): BufferedImage {
    val w = this.framebufferWidth
    val h = this.framebufferHeight
    val i = w * h
    val pixelBuffer = BufferUtils.createIntBuffer(i)
    val pixelValues = IntArray(i)
    GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1)
    GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1)
    GlStateManager.bindTexture(this.framebufferTexture)
    GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer)
    pixelBuffer[pixelValues]
    TextureUtil.processPixelValues(pixelValues, w, h)
    val bufferedimage = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val j = this.framebufferTextureHeight - this.framebufferHeight
    for (k in j until this.framebufferTextureHeight) {
        for (l in 0 until this.framebufferWidth) {
            bufferedimage.setRGB(l, k - j, pixelValues[k * this.framebufferTextureWidth + l])
        }
    }
    return bufferedimage
}