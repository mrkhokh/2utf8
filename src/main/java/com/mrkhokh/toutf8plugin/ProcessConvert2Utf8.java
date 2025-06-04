package com.mrkhokh.toutf8plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ProcessConvert2Utf8 extends AnAction {
    public static final String WINDOWS_1251 = "Windows-1251";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        final VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (project == null || selectedFile == null) {
            System.out.println("There is nothing to process, no projects provided, no java classes selected.");
            return;
        }
        List<VirtualFile> filesToProcess = new ArrayList<>();
        if (selectedFile.isDirectory()) {
            collectJavaFiles(selectedFile, filesToProcess);
        } else if (isJavaFile(selectedFile)) {
            filesToProcess.add(selectedFile);
        }

        if (filesToProcess.isEmpty()) {
            Messages.showInfoMessage("No .java files found to process.", "No Files");
            return;
        }

        new Task.Backgroundable(project, "Converting java files to utf-8", true) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                indicator.setText("Starting file processing...");

                int totalFiles = filesToProcess.size();
                for (int i = 0; i < totalFiles; i++) {
                    if (indicator.isCanceled()) return;

                    final VirtualFile file = filesToProcess.get(i);
                    indicator.setText("Processing file " + file.getName());
                    indicator.setFraction((double) i / (double) totalFiles);

                    Mode2.processingByMode2ConvertAndTest(file.getPath());
                }
            }

            @Override
            public void onSuccess() {
                Messages.showInfoMessage("All files have been processed successfully", "Success");
            }

            @Override
            public void onCancel() {
                Messages.showInfoMessage("File processing was cancelled by the User", "Cancelled");
            }
        }.queue();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean isEnabled = file != null && file.isDirectory() || isJavaFile(file);
        e.getPresentation().setEnabledAndVisible(isEnabled);
    }

    private void collectJavaFiles(VirtualFile file, List<VirtualFile> result) {
        for (VirtualFile child : file.getChildren()) {
            if (child.isDirectory()) {
                collectJavaFiles(child, result);
            } else if (isJavaFile(child)) {
                result.add(child);
            }
        }
    }

    private boolean isJavaFile(VirtualFile file) {
        return file != null && !file.isDirectory() && "java".equals(file.getExtension());
    }
}
