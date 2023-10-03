package com.example.kotlin.ast

import java.io.BufferedInputStream

object Utils {
    fun getContentFromGit(rev: String, filename: String): String {
        val cmd = listOf(
            "git",
            "cat-file",
            "--textconv",
            "$rev:$filename"
        )

        return getOutput(cmd)
    }

    fun getMergeBase(rev1: String, rev2: String): String {
        val cmd = listOf(
            "git",
            "merge-base",
            rev1, rev2
        )

        return getOutput(cmd).trim()
    }

    fun getChangedFiles(rev1: String, rev2: String): List<String> {
        val cmd = listOf(
            "git",
            "diff",
            "--name-only",
            rev1,
            rev2
        )
        val pb = ProcessBuilder(cmd)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
        pb.start().inputStream.bufferedReader().use {
            return it.readLines().filter { filename ->
                filename.endsWith(".kt")
            }
        }
    }

    fun printDiff(rev1: String, rev2: String, filename: String) {
        val cmd = listOf(
                "git",
                "diff",
                rev1,
                rev2,
                filename
        )
        val out = getOutput(cmd)
        println(out)
    }

    private fun getOutput(cmd: List<String>): String {
        val pb = ProcessBuilder(cmd)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.INHERIT)

        val process = pb.start()
        process.inputStream.bufferedReader().use {
            return it.readText()
        }
    }
}