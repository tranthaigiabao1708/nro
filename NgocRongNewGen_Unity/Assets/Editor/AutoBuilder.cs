using System;
using System.IO;
using UnityEditor;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;
using UnityEngine;

public static class AutoBuilder
{
    [MenuItem("NRO Admin/Build Client PC Ngoc Rong NewGen (Ra NgocRongNewGen_Client)")]
    public static void BuildWindowsMenu()
    {
        BuildWindowsInternal(false);
    }

    public static void BuildWindows()
    {
        BuildWindowsInternal(true);
    }

    private static void BuildWindowsInternal(bool exitAfterBuild)
    {
        try
        {
            string[] scenePaths = new string[]
            {
                "Assets/Scenes/NROL.unity",
                "Assets/Scenes/NROL1.unity"
            };

            EditorBuildSettings.scenes = new EditorBuildSettingsScene[]
            {
                new EditorBuildSettingsScene("Assets/Scenes/NROL.unity", true),
                new EditorBuildSettingsScene("Assets/Scenes/NROL1.unity", true)
            };

            PlayerSettings.companyName = "NgocRongNewGen";
            PlayerSettings.productName = "Ngọc Rồng NewGen";
            PlayerSettings.runInBackground = true;
            PlayerSettings.fullScreenMode = FullScreenMode.Windowed;
            PlayerSettings.defaultScreenWidth = 1024;
            PlayerSettings.defaultScreenHeight = 600;
            PlayerSettings.resizableWindow = true;
            PlayerSettings.SetScriptingBackend(NamedBuildTarget.Standalone, ScriptingImplementation.Mono2x);

            // Xuất ra thư mục NgocRongNewGen_Client nằm cạnh thư mục project Unity (không phụ thuộc ổ đĩa)
            string outDir = Path.GetFullPath(Path.Combine(Application.dataPath, "..", "..", "NgocRongNewGen_Client"));
            if (!Directory.Exists(outDir))
            {
                Directory.CreateDirectory(outDir);
            }
            else if (File.Exists(Path.Combine(outDir, "GameAssembly.dll")))
            {
                // Dọn bản IL2CPP cũ (giữ nguyên server_ip.txt) để tránh xung đột Mono2x vs IL2CPP
                string backupDir = Path.Combine(Path.GetTempPath(), "NroOldClient_" + DateTime.Now.Ticks);
                Directory.CreateDirectory(backupDir);
                string[] oldFiles = { "GameAssembly.dll", "baselib.dll", "Ngoc Rong.exe", "UnityPlayer.dll", "UnityCrashHandler64.exe" };
                foreach (string f in oldFiles)
                {
                    string fp = Path.Combine(outDir, f);
                    if (File.Exists(fp))
                    {
                        try { File.Delete(fp); }
                        catch { File.Move(fp, Path.Combine(backupDir, f)); }
                    }
                }
                string oldDataDir = Path.Combine(outDir, "Ngoc Rong_Data");
                if (Directory.Exists(oldDataDir))
                {
                    try { Directory.Delete(oldDataDir, true); }
                    catch { Directory.Move(oldDataDir, Path.Combine(backupDir, "Ngoc Rong_Data")); }
                }
            }

            string exePath = Path.Combine(outDir, "NgocRongNewGen.exe");
            BuildPlayerOptions buildOptions = new BuildPlayerOptions
            {
                scenes = scenePaths,
                locationPathName = exePath,
                target = BuildTarget.StandaloneWindows64,
                options = BuildOptions.None
            };

            Debug.Log("[AutoBuilder] Starting Windows x64 build -> " + exePath);
            BuildReport report = BuildPipeline.BuildPlayer(buildOptions);
            BuildSummary summary = report.summary;

            if (summary.result == BuildResult.Succeeded)
            {
                Debug.Log("[AutoBuilder] BUILD SUCCEEDED! Size bytes: " + summary.totalSize);
                if (exitAfterBuild) EditorApplication.Exit(0);
                else EditorUtility.DisplayDialog("Thành công", "Đã xuất bản NgocRongNewGen.exe tại:\n" + outDir, "OK");
            }
            else
            {
                Debug.LogError("[AutoBuilder] BUILD FAILED: " + summary.result + " | Errors: " + summary.totalErrors);
                if (exitAfterBuild) EditorApplication.Exit(1);
            }
        }
        catch (Exception ex)
        {
            Debug.LogError("[AutoBuilder] EXCEPTION: " + ex);
            if (exitAfterBuild) EditorApplication.Exit(2);
        }
    }
}
