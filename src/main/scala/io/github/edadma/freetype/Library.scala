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

// 16.16 fixed-point conversions for the variation API's design coordinates and axis ranges.
// FT_Fixed is a C `long` (word-sized here), so a Scala Long is converted through `.toSize`
// rather than cast directly — a raw `asInstanceOf` from a boxed Long throws at runtime.
private def fixedToDouble(f: FT_Fixed): Double = f.toLong.toDouble / 65536.0
private def doubleToFixed(d: Double): FT_Fixed = math.round(d * 65536.0).toSize.asInstanceOf[FT_Fixed]

implicit class Library(val libraryptr: FT_Library) extends AnyVal:
  def doneFreeType: Int = FT_Done_FreeType(libraryptr)

  /** Release the variation descriptor obtained from [[Face.getMMVar]]. It is owned by the
    * library, so it is freed here rather than through the face. */
  def doneMMVar(mmvar: MMVar): Int = FT_Done_MM_Var(libraryptr, mmvar.ptr)
  def newFace(filepathname: String, face_index: Long): Either[Int, Face] =
    val aface = stackalloc[FT_Face]()

    Zone { FT_New_Face(libraryptr, toCString(filepathname), face_index.toSize.asInstanceOf[FT_Long], aface) } match
      case 0   => Right(!aface)
      case err => Left(err)

  /** Open a face from a font already in memory rather than from a file — for fonts embedded
    * in the binary or otherwise held as bytes. `buffer` must stay alive and unchanged for
    * the whole lifetime of the returned face (FreeType reads from it lazily); `size` is its
    * length in bytes and `face_index` selects a face within a collection (0 for a plain
    * font). The buffer is not copied. */
  def newMemoryFace(buffer: Ptr[Byte], size: Long, face_index: Long): Either[Int, Face] =
    val aface = stackalloc[FT_Face]()

    FT_New_Memory_Face(
      libraryptr,
      buffer,
      size.toSize.asInstanceOf[FT_Long],
      face_index.toSize.asInstanceOf[FT_Long],
      aface,
    ) match
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
    FT_Get_Char_Index(faceptr, charcode.toUSize.asInstanceOf[FT_ULong]).toInt
  def getKerning(leftChar: Char, rightChar: Char, mode: KerningMode = KerningMode.DEFAULT): Double =
    val leftIdx = FT_Get_Char_Index(faceptr, leftChar.toLong.toUSize.asInstanceOf[FT_ULong])
    val rightIdx = FT_Get_Char_Index(faceptr, rightChar.toLong.toUSize.asInstanceOf[FT_ULong])
    val vec = stackalloc[FT_Vector]()
    val err = FT_Get_Kerning(faceptr, leftIdx, rightIdx, mode.ordinal.toUInt, vec)
    if err != 0 then
      println(s"FT_Get_Kerning error: $err (${errorString(err)})")
      0.0
    else
      val x = vec._1.toLong.toDouble
      if mode == KerningMode.UNSCALED then x else x / 64.0 // 26.6 fixed-point for scaled modes

  /** The face's variation descriptor — its axes (weight, width, slant, …) and named instances —
    * for a variable (OpenType `fvar`) font, or `Left(error)` for a static font. The returned
    * [[MMVar]] is owned by the library; pass it to [[Library.doneMMVar]] when finished. Read an
    * axis's tag and range from it to know how to drive [[setVarDesignCoordinates]]. */
  def getMMVar: Either[FT_Error, MMVar] =
    val amaster = stackalloc[Ptr[FT_MM_Var]]()
    FT_Get_MM_Var(faceptr, amaster) match
      case 0   => Right(MMVar(!amaster))
      case err => Left(err)

  /** Set the face's variation design coordinates — one value per axis, in the axis's own design
    * units (e.g. `400` on a `wght` axis), in the order the axes appear in [[getMMVar]]. This
    * reshapes the outlines the next time a glyph is loaded, which is how a single variable font
    * renders any weight or width. Returns 0 on success. */
  def setVarDesignCoordinates(coords: Seq[Double]): FT_Error =
    val n   = coords.length
    val arr = stackalloc[FT_Fixed](n.toUInt)
    var i   = 0
    while i < n do
      arr(i) = doubleToFixed(coords(i))
      i += 1
    FT_Set_Var_Design_Coordinates(faceptr, n.toUInt, arr)

  /** Read back the current design coordinates for the face's first `numAxes` axes. */
  def getVarDesignCoordinates(numAxes: Int): Either[FT_Error, Vector[Double]] =
    val arr = stackalloc[FT_Fixed](numAxes.toUInt)
    FT_Get_Var_Design_Coordinates(faceptr, numAxes.toUInt, arr) match
      case 0   => Right((0 until numAxes).map(i => fixedToDouble(arr(i))).toVector)
      case err => Left(err)

  /** Select one of the font's predefined named instances (e.g. "Bold", "Condensed") by index,
    * a shortcut for setting that instance's coordinates. Index 0 resets to the default. */
  def setNamedInstance(index: Int): FT_Error = FT_Set_Named_Instance(faceptr, index.toUInt)

implicit class Bitmap(val bitmapptr: Ptr[FT_Bitmap]) extends AnyVal:
  def rows: Int = bitmapptr._1.toInt
  def width: Int = bitmapptr._2.toInt
  def pitch: Int = bitmapptr._3
  def buffer(idx: Int): Int = (!(bitmapptr._4 + idx)).toInt & 0xff

// The variation descriptor of a variable font (from [[Face.getMMVar]]): how many axes it has
// and access to each. Free it with [[Library.doneMMVar]] when done.
class MMVar(val ptr: Ptr[FT_MM_Var]) extends AnyVal:
  def numAxis: Int        = ptr._1.toInt
  def numDesigns: Int     = ptr._2.toInt
  def numNamedStyles: Int = ptr._3.toInt
  def axis(i: Int): VarAxis = VarAxis(ptr._4 + i)

// One variation axis: its name, its design-value range, and its OpenType tag (the four-letter
// id like "wght" or "wdth" used to recognise it).
class VarAxis(val ptr: Ptr[FT_Var_Axis]) extends AnyVal:
  def name: String     = fromCString(ptr._1)
  def minimum: Double  = fixedToDouble(ptr._2)
  def default: Double  = fixedToDouble(ptr._3)
  def maximum: Double  = fixedToDouble(ptr._4)
  def tag: Long        = ptr._5.toLong
  /** The axis tag decoded to its four-character string, e.g. "wght". */
  def tagString: String =
    val t = tag
    String(Array(((t >> 24) & 0xff).toChar, ((t >> 16) & 0xff).toChar, ((t >> 8) & 0xff).toChar, (t & 0xff).toChar))

def errorString(error_code: Int): String = fromCString(FT_Error_String(error_code))
