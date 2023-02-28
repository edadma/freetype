package io.github.edadma.freetype

import scala.scalanative.unsafe._

import extern.LibFreeType as lib

implicit class Library(val library: lib.FT_Library) extends AnyVal:
  def doneFreeType: Int = lib.FT_Done_FreeType(library)
