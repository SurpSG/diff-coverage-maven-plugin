package com.github.surpsg.diffsource

import org.eclipse.jgit.lib.CoreConfig

fun getCrlf(lineSeparator: String = System.lineSeparator()): CoreConfig.AutoCRLF {
    return if (lineSeparator == "\r\n") {
        CoreConfig.AutoCRLF.TRUE
    } else {
        CoreConfig.AutoCRLF.INPUT
    }
}
