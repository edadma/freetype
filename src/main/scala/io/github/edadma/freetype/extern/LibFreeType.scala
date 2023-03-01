package io.github.edadma.freetype.extern

import scala.scalanative.unsafe._

@link("freetype")
@extern
object LibFreeType:
  type FT_Library = Ptr[Byte]
  type FT_Error = CInt
  type FT_Long = CLong
  type FT_Face = Ptr[CStruct0]
  type FT_UInt = CUnsignedInt
  type FT_ULong = CUnsignedLong
  type FT_Int32 = CInt
  type FT_UInt32 = CUnsignedInt
  type FT_GlyphSlot = Ptr[CStruct0]
  type FT_Render_Mode = CInt

  def FT_Init_FreeType(alibrary: FT_Library): FT_Error = extern
  def FT_Done_FreeType(alibrary: FT_Library): FT_Error = extern
  def FT_New_Face(library: FT_Library, filepathname: CString, face_index: FT_Long, aface: Ptr[FT_Face]): FT_Error =
    extern
  def FT_Done_Face(face: FT_Face): FT_Error = extern
  def FT_Error_String(error_code: FT_Error): CString = extern
  def FT_Set_Pixel_Sizes(face: FT_Face, pixel_width: FT_UInt, pixel_height: FT_UInt): FT_Error = extern
  def FT_Load_Char(face: FT_Face, char_code: FT_ULong, load_flags: FT_Int32): FT_Error = extern
  def FT_Render_Glyph(slot: FT_GlyphSlot, render_mode: FT_Render_Mode): FT_Error = extern
