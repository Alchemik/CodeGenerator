package me.lotabout.codegenerator.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.CodeGeneratorWorker;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.GenerationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.GenerateToStringUtils;
import org.jetbrains.java.generate.config.Config;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;

public class CodeGeneratorActionHandler implements CodeInsightActionHandler {

    private static final Logger logger = Logger.getInstance(CodeGeneratorActionHandler.class);

    private CodeGeneratorSettings settings;

    private String templateId;

    CodeGeneratorActionHandler(String templateId) {
        this.settings = ServiceManager.getService(CodeGeneratorSettings.class);;
        this.templateId = templateId;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
        if (project == null) {
            return;
        }

        PsiClass clazz = getSubjectClass(editor, psiFile);
        assert clazz != null;

        doExecuteAction(project, clazz, editor);
    }

    private void doExecuteAction(@NotNull final Project project, @NotNull final PsiClass clazz, final Editor editor) {
        if (!FileModificationService.getInstance().preparePsiElementsForWrite(clazz)) {
            return;
        }

        CodeTemplate codeTemplate = settings.getCodeTemplate(templateId).orElseThrow(IllegalStateException::new);

        logger.debug("+++ doExecuteAction - START +++");
        if (logger.isDebugEnabled()) {
            logger.debug("Current project " + project.getName());
        }

        final PsiElementClassMember[] dialogMembers = buildMembersToShow(clazz, codeTemplate);

        final MemberChooser<PsiElementClassMember> chooser =
                new MemberChooser<PsiElementClassMember>(dialogMembers, true, true, project, PsiUtil.isLanguageLevel5OrHigher(clazz), new JPanel(new BorderLayout())) {
                    @Nullable @Override protected String getHelpId() {
                        return "editing.altInsert.codegenerator";
                    }
                };
        chooser.setTitle("Selection Fields for code generation");
        chooser.setCopyJavadocVisible(false);
        chooser.selectElements(getPreselection(clazz, dialogMembers));
        chooser.show();

        if (DialogWrapper.OK_EXIT_CODE == chooser.getExitCode()) {
            Collection<PsiMember> selectedMembers = GenerationUtil.convertClassMembersToPsiMembers(chooser.getSelectedElements());

            final CodeGeneratorWorker worker = new CodeGeneratorWorker(clazz, editor, codeTemplate);
            worker.execute(selectedMembers, codeTemplate.template);
        }
        logger.debug("+++ doExecuteAction - END +++");
    }

    @Nullable
    private static PsiClass getSubjectClass(Editor editor, final PsiFile file) {
        if (file == null) return null;

        int offset = editor.getCaretModel().getOffset();
        PsiElement context = file.findElementAt(offset);

        if (context == null) return null;

        PsiClass clazz = PsiTreeUtil.getParentOfType(context, PsiClass.class, false);
        if (clazz == null) {
            return null;
        }

        return clazz;
    }

    public static PsiElementClassMember[] buildMembersToShow(PsiClass clazz, CodeTemplate codeTemplate) {
        Config config = generatorConfig2Config(codeTemplate);

        PsiField[] filteredFields = GenerateToStringUtils.filterAvailableFields(clazz, true, config.getFilterPattern());
        if (logger.isDebugEnabled()) logger.debug("Number of fields after filtering: " + filteredFields.length);
        PsiMethod[] filteredMethods;
        if (config.enableMethods) {
            // filter methods as it is enabled from config
            filteredMethods = GenerateToStringUtils.filterAvailableMethods(clazz, config.getFilterPattern());
            if (logger.isDebugEnabled()) logger.debug("Number of methods after filtering: " + filteredMethods.length);
        } else {
            filteredMethods = PsiMethod.EMPTY_ARRAY;
        }

        return GenerationUtil.combineToClassMemberList(filteredFields, filteredMethods);
    }

    private static PsiElementClassMember[] getPreselection(@NotNull PsiClass clazz, PsiElementClassMember[] dialogMembers) {
        return Arrays.stream(dialogMembers)
                .filter(member -> member.getElement().getContainingClass() == clazz)
                .toArray(PsiElementClassMember[]::new);
    }

    private static Config generatorConfig2Config(CodeTemplate codeTemplate) {
        Config config = new Config();
        config.useFullyQualifiedName = codeTemplate.useFullyQualifiedName;
        config.insertNewMethodOption = codeTemplate.insertNewMethodOption;
        config.whenDuplicatesOption = codeTemplate.whenDuplicatesOption;
        config.filterConstantField = codeTemplate.filterConstantField;
        config.filterEnumField = codeTemplate.filterEnumField;
        config.filterTransientModifier = codeTemplate.filterTransientModifier;
        config.filterStaticModifier = codeTemplate.filterStaticModifier;
        config.filterFieldName = codeTemplate.filterFieldName;
        config.filterMethodName = codeTemplate.filterMethodName;
        config.filterMethodType = codeTemplate.filterMethodType;
        config.filterFieldType = codeTemplate.filterFieldType;
        config.filterLoggers = codeTemplate.filterLoggers;
        config.enableMethods = codeTemplate.enableMethods;
        config.jumpToMethod = codeTemplate.jumpToMethod;
        config.sortElements = codeTemplate.sortElements;
        return config;
    }

}
