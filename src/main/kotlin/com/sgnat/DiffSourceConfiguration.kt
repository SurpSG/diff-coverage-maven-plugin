package com.sgnat

import java.io.File
import java.net.URL

class DiffSourceConfiguration(
    var url: URL? = null,
    var git: String? = null,
    var file: File? = null
) {
    override fun toString(): String {
        return "DiffSource(git=$git, file=$file, url=$url)"
    }
}
