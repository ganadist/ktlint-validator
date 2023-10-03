package com.example.kotlin.ast

import com.google.common.truth.Truth
import org.jetbrains.kotlin.psi.psiUtil.getTextWithLocation
import kotlin.system.exitProcess

class KtCompare(
    private val rev1: String,
    private val rev2: String,
    private val filename: String
): Runnable {
    override fun run() {
        if (filename in dontCheckFiles) {
            return
        }

        val contentFromGit = Utils.getContentFromGit(rev1, filename)
        val astIteratorFromGit = KotlinAstIterator.getIterator(filename, contentFromGit)
        val contentFromHead = Utils.getContentFromGit(rev2, filename)
        val astIteratorFromHead = KotlinAstIterator.getIterator(filename, contentFromHead)

        println("checking: $filename")
        for ((node1, node2) in astIteratorFromGit.zip(astIteratorFromHead)) {
            try {
                Truth.assertThat(node1.text).contains(node2.text)
            } catch (th: Throwable) {
                synchronized(lock) {
                    th.printStackTrace()

                    Utils.printDiff(rev1, rev2, filename)
                    println("\nnode1: $node1, node2: $node2")
                    println("error ${node2.psi.getTextWithLocation()}\n")
                }
                if (filename in ignoreFiles) {
                    continue
                }
                exitProcess(-1)
            }
        }
    }

    companion object {
        private val lock = Any()
        private val dontCheckFiles = listOf<String>(
        )
        private val ignoreFiles = listOf<String>(
        )
    }
}
