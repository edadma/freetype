package io.github.edadma.freetype

@main def run(): Unit =
  val library = initFreeType.getOrElse(sys.error("error initializing library"))
