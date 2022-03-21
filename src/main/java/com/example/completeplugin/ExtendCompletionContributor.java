package com.example.completeplugin;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.compiler.chainsSearch.completion.MethodChainCompletionContributor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.PsiJavaPatterns.psiReferenceExpression;
import static com.intellij.patterns.StandardPatterns.or;

public class ExtendCompletionContributor extends CompletionContributor {
    private static final Logger LOG = Logger.getInstance(ExtendCompletionContributor.class);

    public ExtendCompletionContributor() {
        ElementPattern<PsiElement> pattern = or(patternForMethodCallArgument(), patternForVariableAssignment(), patternForReturnExpression());
        extend(CompletionType.BASIC, pattern,
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        System.out.println(123);
                        result.addElement(LookupElementBuilder.create("hello"));
                    }
                }
        );
    }

    @NotNull
    private static ElementPattern<PsiElement> patternForMethodCallArgument() {
        return psiElement().withSuperParent(3, PsiMethodCallExpression.class).withParent(psiReferenceExpression().with(
                new PatternCondition<>("QualifierIsNull") {
                    @Override
                    public boolean accepts(@NotNull PsiReferenceExpression referenceExpression, ProcessingContext context) {
                        return referenceExpression.getQualifierExpression() == null;
                    }
                }));
    }

    @NotNull
    private static ElementPattern<PsiElement> patternForVariableAssignment() {
        final ElementPattern<PsiElement> patternForParent = or(PsiJavaPatterns.psiElement().withText(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED)
                .afterSiblingSkipping(PsiJavaPatterns.psiElement(PsiWhiteSpace.class),
                        PsiJavaPatterns.psiElement(PsiJavaToken.class).withText("=")));

        return PsiJavaPatterns.psiElement().withParent(patternForParent).withSuperParent(2, or(PsiJavaPatterns.psiElement(PsiAssignmentExpression.class),
                PsiJavaPatterns.psiElement(PsiLocalVariable.class)
                        .inside(PsiDeclarationStatement.class))).inside(PsiMethod.class);
    }

    private static ElementPattern<PsiElement> patternForReturnExpression() {
        return psiElement().withParent(psiElement(PsiReferenceExpression.class).withText(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)).withSuperParent(2, PsiReturnStatement.class);
    }
}
