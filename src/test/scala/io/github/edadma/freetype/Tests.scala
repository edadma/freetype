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

}
