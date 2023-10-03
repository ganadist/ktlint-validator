package com.example.kotlin.ast

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiComment
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFileAnnotationList
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.getTextWithLocation
import java.io.File
import java.lang.RuntimeException
import java.util.*

private val KOTLIN_PSI_FILE_FACTORY_PROVIDER = KotlinPsiFileFactoryProvider()

class KotlinAstIterator(private var root: ASTNode?): Iterable<ASTNode>, Iterator<ASTNode> {
    private val visiting = Stack<ASTNode>()

    override fun iterator(): Iterator<ASTNode> {
        return this
    }

    override fun hasNext(): Boolean {
        return root != null
    }

    /**
     * Implemented pre-order traversal
     */
    override fun next(): ASTNode {
        if (!hasNext()) {
            throw NoSuchElementException("no more element")
        }

        if (visiting.isEmpty()) {
            visiting.push(root)
        }

        val node = visiting.pop()
        val children = node.children().asIterable().reversed().filter {
            when (it.psi) {
                // Ignore whitespaces and its children
                is PsiWhiteSpace -> false
                // Ignore comments and its children
                is PsiComment -> false
                // Ignore "@file" annotations and its children
                is KtFileAnnotationList -> false
                // Ignore "@Suppress" annotations and its children
                is KtAnnotationEntry -> {
                    !it.text.startsWith("@Suppress")
                }
                else -> true
            }
        }

        visiting.addAll(children)

        if (visiting.isEmpty()) {
            root = null
        }

        // Ignore root node itself
        if (node == root) {
            return next()
        }

        // Ignore composite elements, but children still will be visited.
        if (node is CompositeElement) {
            return next()
        }

        return node
    }

    companion object {
        internal fun getIterator(file: File): KotlinAstIterator {
            val content = file.readText(charset = Charsets.UTF_8)
            return getIterator(file.canonicalPath, content)
        }

        internal fun getIterator(filename: String, content: String): KotlinAstIterator {
            val psiFileFactory = KOTLIN_PSI_FILE_FACTORY_PROVIDER.getKotlinPsiFileFactory()

            val psiFile = psiFileFactory.createFileFromText(
                filename,
                KotlinLanguage.INSTANCE,
                content
            ) as KtFile

            psiFile.findErrorElement()?.let {
                throw RuntimeException("$filename : ${it.getTextWithLocation()} ")
            }

            return KotlinAstIterator(psiFile.node)
        }

        private fun PsiElement.findErrorElement(): PsiErrorElement? {
            if (this is PsiErrorElement) {
                return this
            }
            this.children.forEach { child ->
                val errorElement = child.findErrorElement()
                if (errorElement != null) {
                    return errorElement
                }
            }
            return null
        }
    }
}