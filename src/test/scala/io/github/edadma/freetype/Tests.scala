package io.github.edadma.freetype

import scala.scalanative.unsafe.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class Tests extends AnyFreeSpec with Matchers {

  "newMemoryFace rejects bytes that are not a font" in {
    // Exercises the in-memory face loader end to end: init the library, hand FreeType a
    // buffer of non-font bytes, and confirm it reports an error rather than succeeding. A
    // deterministic check that needs no font file on disk.
    val lib = initFreeType match
      case Right(l) => l
      case Left(e)  => fail(s"FreeType init failed: $e")

    Zone {
      val n   = 64
      val buf = alloc[Byte](n) // zeroed — not a valid font
      lib.newMemoryFace(buf, n.toLong, 0) match
        case Left(err) => err should not be 0
        case Right(_)  => fail("expected an error for non-font bytes")
    }

    lib.doneFreeType
  }

  "setting variation design coordinates converts to 16.16 fixed point without a cast error" in {
    // FT_Fixed is a word-sized C long, so a Scala Long has to be converted through `.toSize`;
    // a raw `asInstanceOf` threw a ClassCastException here before the fix. KaiseiDecol is a
    // static font, so FreeType reports an error rather than applying a variation — but the
    // fixed-point conversion still runs first, which is exactly the path being pinned: it must
    // return rather than crash.
    val lib = initFreeType match
      case Right(l) => l
      case Left(e)  => fail(s"FreeType init failed: $e")

    val face = lib.newFace("KaiseiDecol/KaiseiDecol-Regular.ttf", 0) match
      case Right(f) => f
      case Left(e)  => fail(s"cannot load font: $e")

    val err = face.setVarDesignCoordinates(Seq(400.0, 14.0))
    err should not be 0 // a static font has no axes to set

    face.doneFace
    lib.doneFreeType
  }

}
