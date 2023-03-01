package io.github.edadma.freetype

import scala.scalanative.unsafe._

import io.github.edadma.freetype.extern.LibFreeType.*

implicit class Library(val libraryptr: FT_Library) extends AnyVal:
  def doneFreeType: Int = FT_Done_FreeType(libraryptr)
  def newFace(filepathname: String, face_index: Long): Either[Int, Face] =
    val aface = stackalloc[FT_Face]()

    Zone(implicit z => FT_New_Face(libraryptr, toCString(filepathname), face_index, aface)) match
      case 0   => Right(!aface)
      case err => Left(err)

implicit class Face(val faceptr: FT_Face) extends AnyVal:
  def DoneFace: Int = FT_Done_Face(faceptr)

def errorString(error_code: Int): String = fromCString(FT_Error_String(error_code))
