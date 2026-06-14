package io.github.edadma.freetype.extern

import scala.scalanative.unsafe._

import io.github.edadma.freetype_face.FT_Face

@link("freetype")
@extern
object LibFreeType:
  type FT_Library = Ptr[Byte]
  type FT_Error = CInt
  type FT_Long = CLong
  type FT_UInt = CUnsignedInt
  type FT_ULong = CUnsignedLong
  type FT_Int32 = CInt
  type FT_UInt32 = CUnsignedInt
  type FT_GlyphSlot = Ptr[CStruct0]
  type FT_Render_Mode = CInt
  type FT_Pos = CLong
  type FT_Vector = CStruct2[FT_Pos, FT_Pos]

  // 16.16 fixed-point — the design-coordinate and axis-range unit of the variation API.
  type FT_Fixed = CLong
  // One variation axis: name, minimum / default / maximum design value, the four-byte
  // OpenType tag (e.g. 'wght'), and the string id of its name.
  type FT_Var_Axis = CStruct6[CString, FT_Fixed, FT_Fixed, FT_Fixed, FT_ULong, FT_UInt]
  // A named instance: its per-axis coordinates plus the string ids of its names.
  type FT_Var_Named_Style = CStruct3[Ptr[FT_Fixed], FT_UInt, FT_UInt]
  // The variation descriptor of a font: axis/design/named-style counts and the axis and
  // named-style arrays.
  type FT_MM_Var = CStruct5[FT_UInt, FT_UInt, FT_UInt, Ptr[FT_Var_Axis], Ptr[FT_Var_Named_Style]]
  type FT_Bitmap = CStruct8[
    /* rows */ CUnsignedInt,
    /* width */ CUnsignedInt,
    /* pitch */ CInt,
    /* buffer */ Ptr[CUnsignedChar],
    /* num_grays */ CUnsignedShort,
    /* pixel_mode */ CUnsignedChar,
    /* palette_mode */ CUnsignedChar,
    /* palette */ Ptr[Byte],
  ]

  def FT_Init_FreeType(alibrary: Ptr[FT_Library]): FT_Error = extern
  def FT_Done_FreeType(alibrary: FT_Library): FT_Error = extern
  def FT_New_Face(library: FT_Library, filepathname: CString, face_index: FT_Long, aface: Ptr[FT_Face]): FT_Error =
    extern
  def FT_New_Memory_Face(
      library:    FT_Library,
      file_base:  Ptr[Byte],
      file_size:  FT_Long,
      face_index: FT_Long,
      aface:      Ptr[FT_Face],
  ): FT_Error = extern
  def FT_Done_Face(face: FT_Face): FT_Error = extern
  def FT_Error_String(error_code: FT_Error): CString = extern
  def FT_Set_Pixel_Sizes(face: FT_Face, pixel_width: FT_UInt, pixel_height: FT_UInt): FT_Error = extern
  def FT_Load_Char(face: FT_Face, char_code: FT_ULong, load_flags: FT_Int32): FT_Error = extern
  def FT_Render_Glyph(slot: FT_GlyphSlot, render_mode: FT_Render_Mode): FT_Error = extern
  def FT_Get_Char_Index(face: FT_Face, charcode: FT_ULong): FT_UInt = extern
  def FT_Get_Kerning(face: FT_Face, left_glyph: FT_UInt, right_glyph: FT_UInt, kern_mode: FT_UInt, akerning: Ptr[FT_Vector]): FT_Error = extern
  def FT_Get_MM_Var(face: FT_Face, amaster: Ptr[Ptr[FT_MM_Var]]): FT_Error = extern
  def FT_Done_MM_Var(library: FT_Library, amaster: Ptr[FT_MM_Var]): FT_Error = extern
  def FT_Set_Var_Design_Coordinates(face: FT_Face, num_coords: FT_UInt, coords: Ptr[FT_Fixed]): FT_Error = extern
  def FT_Get_Var_Design_Coordinates(face: FT_Face, num_coords: FT_UInt, coords: Ptr[FT_Fixed]): FT_Error = extern
  def FT_Set_Named_Instance(face: FT_Face, instance_index: FT_UInt): FT_Error = extern
  def FT_Load_Sfnt_Table(face: FT_Face, tag: FT_ULong, offset: FT_Long, buffer: Ptr[Byte], length: Ptr[FT_ULong]): FT_Error = extern
