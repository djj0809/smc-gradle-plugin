package com.fsryan.gradle.smc

class SmCompiler {

    private File srcDir
    private String smcJarFile
    private String smSrcDir
    private SmOutputDirFinder outputDirFinder
    private int graphVizLevel
    private boolean generateHtmlTable

    SmCompiler(File srcDir, String smcJarFile, SmOutputDirFinder outputDirFinder, SmcExtension smc) {
        this(srcDir, smcJarFile, outputDirFinder, smc.smSrcDir, smc.graphVizLevel, smc.outputHtmlTable)
    }

    SmCompiler(File srcDir, String smcJarFile, SmOutputDirFinder outputDirFinder, String smSrcDir, int graphVizLevel, boolean generateHtmlTable) {
        this.srcDir = srcDir
        this.smcJarFile = smcJarFile
        this.outputDirFinder = outputDirFinder
        this.smSrcDir = smSrcDir
        this.graphVizLevel = Math.min(2, graphVizLevel)
        this.generateHtmlTable = generateHtmlTable
    }

    void execute() {
        String searchDir = srcDir.parentFile.absolutePath + File.separator + smSrcDir

        File searchDirFile = new File(searchDir)
        if (searchDirFile.exists() && !searchDirFile.isDirectory()) {
            println "search dir (" + searchDir + ") does not exist"
            return
        }

        try {
            List<String> smFiles = new FileNameByRegexFinder().getFileNames(searchDir, /.*\.sm/)
            for (String smFile : smFiles) {
                createOutputs(new SmcCommander(smcJarFile, smFile, javaOutputDir(smFile), outputDirFinder.getArtifactOutputDir()))
            }
        } catch (FileNotFoundException fnfe) {
            println "not found: " + searchDir
        }
    }

    private void createOutputs(SmcCommander commander) {
        commander.generateStateMachine().execute()
        if (generateDotFile()) {
            commander.generateDotFile(graphVizLevel).execute()
        }
        if (generateHtmlTable) {
            commander.generateHtmlTable().execute()
        }
    }

    private boolean generateDotFile() {
        return graphVizLevel >= 0
    }

    private File javaOutputDir(String smFilePath) {
        File smFileParent = new File(smFilePath).parentFile
        String packagePath = smFileParent.absolutePath.substring(srcDir.absolutePath.length() - 1)
        File ret = new File(outputDirFinder.getSrcOutputDir(), packagePath)
        return ret
    }
}
