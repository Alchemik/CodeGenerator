package me.lotabout.codegenerator.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import me.lotabout.codegenerator.CodeGeneratorSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class CodeGeneratorGroup extends ActionGroup implements DumbAware {
    private CodeGeneratorSettings settings;

    public CodeGeneratorGroup() {
        settings = ServiceManager.getService(CodeGeneratorSettings.class);
    }

    @Override
    public boolean hideIfNoVisibleChildren() {
        return true;
    }

    @NotNull @Override public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        if (anActionEvent == null) {
            return AnAction.EMPTY_ARRAY;
        }

        Project project = PlatformDataKeys.PROJECT.getData(anActionEvent.getDataContext());
        if (project == null) {
            return AnAction.EMPTY_ARRAY;
        }

        PsiFile file = anActionEvent.getDataContext().getData(DataKeys.PSI_FILE);
        PsiElement element = file.findElementAt(anActionEvent.getDataContext().getData(LangDataKeys.CARET).getOffset());
        PsiClass clazz = PsiTreeUtil.getParentOfType(element, PsiClass.class, false);
        if (clazz == null) {
            return AnAction.EMPTY_ARRAY;
        }


        String fileName = file.getName();
        final List<AnAction> children = settings.getCodeTemplates()
                .entrySet().stream()
                .filter(entry -> entry.getValue().enabled)
                .filter(entry -> fileName.matches(entry.getValue().fileNamePattern))
                .map(entry -> CodeGeneratorGroup.getOrCreateAction(entry.getKey(), entry.getValue().name))
                .collect(Collectors.toList());

        return children.toArray(new AnAction[children.size()]);
    }

    private static AnAction getOrCreateAction(String templateId, String templateName) {
        final String actionId = "CodeMaker.Menu.Action." + templateId;
        AnAction action = ActionManager.getInstance().getAction(actionId);
        if (action == null) {
            action = new CodeGeneratorAction(templateId, templateName);
            ActionManager.getInstance().registerAction(actionId, action);
        }
        return action;
    }
}
