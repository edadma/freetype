package io.github.edadma.freetype

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import io.github.edadma.freetype.extern.LibFreeType.*
import io.github.edadma.freetype_face.FT_Face

def initFreeType: Either[Int, Library] =
  val alibrary = stackalloc[FT_Library]()

  FT_Init_FreeType(alibrary) match
    case 0   => Right(!alibrary)
    case err => Left(err)

implicit class Library(val libraryptr: FT_Library) extends AnyVal:
  def doneFreeType: Int = FT_Done_FreeType(libraryptr)
  def newFace(filepathname: String, face_index: Long): Either[Int, Face] =
    val aface = stackalloc[FT_Face]()

    Zone { FT_New_Face(libraryptr, toCString(filepathname), face_index.toSize.asInstanceOf[FT_Long], aface) } match
      case 0   => Right(!aface)
      case err => Left(err)

private val FACE_GLYPH = 152
private val FACE_GLYPH_BITMAP = 152

enum RenderMode:
  case NORMAL, LIGHT, MONO, LCD, LCD_V, SDF, MAX

enum KerningMode:
  case DEFAULT, UNFITTED, UNSCALED

implicit class Face(val faceptr: FT_Face) extends AnyVal:
  def doneFace: Int = FT_Done_Face(faceptr)
  def setPixelSizes(pixel_width: Int, pixel_height: Int): Int =
    FT_Set_Pixel_Sizes(faceptr, pixel_width.toUInt, pixel_height.toUInt)
  def loadChar(char_code: Long, load_flags: Int): Int =
    FT_Load_Char(faceptr, char_code.toUSize.asInstanceOf[FT_ULong], load_flags)
  def renderGlyph(render_mode: RenderMode): FT_Error =
    FT_Render_Glyph(
      !(faceptr.asInstanceOf[Ptr[Byte]] + FACE_GLYPH).asInstanceOf[Ptr[FT_GlyphSlot]],
      render_mode.ordinal,
    )
  def bitmap: Bitmap =
    ((!(faceptr.asInstanceOf[Ptr[Byte]] + FACE_GLYPH).asInstanceOf[Ptr[FT_GlyphSlot]])
      .asInstanceOf[Ptr[Byte]] + FACE_GLYPH_BITMAP)
      .asInstanceOf[Ptr[FT_Bitmap]]
  def getCharIndex(charcode: Long): Int =
    FT_Get_Char_Index(faceptr, charcode.toULong.asInstanceOf[FT_ULong]).toInt
  def getKerning(leftChar: Char, rightChar: Char, mode: KerningMode = KerningMode.DEFAULT): Double =
    val leftIdx = FT_Get_Char_Index(faceptr, leftChar.toLong.toULong.asInstanceOf[FT_ULong])
    val rightIdx = FT_Get_Char_Index(faceptr, rightChar.toLong.toULong.asInstanceOf[FT_ULong])
    val vec = stackalloc[FT_Vector]()
    FT_Get_Kerning(faceptr, leftIdx, rightIdx, mode.ordinal.toUInt, vec)
    val x = vec._1.toLong.toDouble
    if mode == KerningMode.UNSCALED then x else x / 64.0 // 26.6 fixed-point for scaled modes

implicit class Bitmap(val bitmapptr: Ptr[FT_Bitmap]) extends AnyVal:
  def rows: Int = bitmapptr._1.toInt
  def width: Int = bitmapptr._2.toInt
  def pitch: Int = bitmapptr._3
  def buffer(idx: Int): Int = (!(bitmapptr._4 + idx)).toInt & 0xff

def errorString(error_code: Int): String = fromCString(FT_Error_String(error_code))
