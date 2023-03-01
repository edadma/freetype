package io.github.edadma.freetype

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import io.github.edadma.freetype.extern.LibFreeType.*

implicit class Library(val libraryptr: FT_Library) extends AnyVal:
  def doneFreeType: Int = FT_Done_FreeType(libraryptr)
  def newFace(filepathname: String, face_index: Long): Either[Int, Face] =
    val aface = stackalloc[FT_Face]()

    Zone(implicit z => FT_New_Face(libraryptr, toCString(filepathname), face_index, aface)) match
      case 0   => Right(!aface)
      case err => Left(err)

private val FACE_GLYPH = 152
private val FACE_GLYPH_BITMAP = 152

implicit class Face(val faceptr: FT_Face) extends AnyVal:
  def doneFace: Int = FT_Done_Face(faceptr)
  def setPixelSizes(pixel_width: Int, pixel_height: Int): Int =
    FT_Set_Pixel_Sizes(faceptr, pixel_width.toUInt, pixel_height.toUInt)
  def loadChar(char_code: Long, load_flags: Int): Int = FT_Load_Char(faceptr, char_code.toULong, load_flags)
  def renderGlyph(render_mode: Int): FT_Error =
    FT_Render_Glyph(!(faceptr + FACE_GLYPH).asInstanceOf[Ptr[FT_GlyphSlot]], render_mode)
  def bitmap(idx: Int): Int =

def errorString(error_code: Int): String = fromCString(FT_Error_String(error_code))
