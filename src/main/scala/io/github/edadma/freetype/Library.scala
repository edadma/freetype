package io.github.edadma.freetype

import scala.scalanative.unsafe._

import io.github.edadma.freetype.extern.LibFreeType as lib

implicit class Library(val library: lib.FT_Library) extends AnyVal:
  def doneFreeType: Int = lib.FT_Done_FreeType(library)
  def FT_New_Face(filepathname: String, face_index: Long): Either[Int, Face] =
    val aface = stackalloc[lib.FT_Face]()

    Zone(implicit z => lib.FT_New_Face(library, toCString(filepathname), face_index, aface)) match
      case 0   => Right(!aface)
      case err => Left(err)

implicit class Face(val library: lib.FT_Face) extends AnyVal

def errorString(error_code: Int): String = fromCString(lib.FT_Error_String(error_code))
