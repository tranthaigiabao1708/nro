using System;
using System.IO;
using UnityEditor;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;
using UnityEngine;

public static class AutoBuilder
{
    [MenuItem("NRO Admin/Build Client PC 2 Tab (Ra C:\\mro\\Client_ChoNguoiChoiTaiVe)")]
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

            PlayerSettings.companyName = "Teamobi2026";
            PlayerSettings.productName = "Ngoc Rong 2026";
            PlayerSettings.runInBackground = true;
            PlayerSettings.fullScreenMode = FullScreenMode.Windowed;
            PlayerSettings.defaultScreenWidth = 1024;
            PlayerSettings.defaultScreenHeight = 600;
            PlayerSettings.resizableWindow = true;
            PlayerSettings.SetScriptingBackend(NamedBuildTarget.Standalone, ScriptingImplementation.Mono2x);

            string outDir = @"C:\mro\Client_ChoNguoiChoiTaiVe";
            if (!Directory.Exists(outDir))
            {
                Directory.CreateDirectory(outDir);
            }

            string exePath = Path.Combine(outDir, "NgocRong2Tab.exe");
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
                else EditorUtility.DisplayDialog("Thành công", "Đã xuất bản NgocRong2Tab.exe tại:\nC:\\mro\\Client_ChoNguoiChoiTaiVe", "OK");
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
