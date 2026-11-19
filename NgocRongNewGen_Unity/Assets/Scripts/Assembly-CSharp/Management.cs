using System;
using System.IO;
using UnityEngine;
using UnityEngine.SceneManagement;

public class Management
{
    public static bool isLogo = true;

    public const string DefaultServer = "Dragon boy Z:127.0.0.1:14445:0,0,0";

    public static string IpServer = GetIpServer();

    public static string LinkWeb = "Dragon boy Z";

    public static bool HasExternalConfig = false;

    public static string GetIpServer()
    {
        try
        {
            string[] candidates = new string[]
            {
                Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "server_ip.txt"),
                Path.Combine(Directory.GetCurrentDirectory(), "server_ip.txt"),
                Application.dataPath != null ? Path.Combine(Path.GetDirectoryName(Application.dataPath), "server_ip.txt") : ""
            };

            foreach (string path in candidates)
            {
                if (!string.IsNullOrEmpty(path) && File.Exists(path))
                {
                    string[] lines = File.ReadAllLines(path);
                    foreach (string raw in lines)
                    {
                        string line = raw.Trim();
                        if (string.IsNullOrEmpty(line) || line.StartsWith("#") || line.StartsWith("//"))
                        {
                            continue;
                        }
                        HasExternalConfig = true;
                        line = line.Replace("Ngọc Rồng NewGen", "Dragon boy Z");
                        if (line.Contains(","))
                        {
                            IpServer = line;
                            return IpServer;
                        }
                        string[] parts = line.Split(':');
                        if (parts.Length == 1)
                        {
                            IpServer = "Dragon boy Z:" + parts[0].Trim() + ":14445:0,0,0";
                            return IpServer;
                        }
                        if (parts.Length == 2)
                        {
                            IpServer = "Dragon boy Z:" + parts[0].Trim() + ":" + parts[1].Trim() + ":0,0,0";
                            return IpServer;
                        }
                        if (parts.Length >= 3)
                        {
                            IpServer = parts[0].Trim() + ":" + parts[1].Trim() + ":" + parts[2].Trim() + ":0,0,0";
                            return IpServer;
                        }
                    }
                }
            }
        }
        catch (Exception)
        {
        }
        IpServer = DefaultServer;
        return IpServer;
    }

    private static TabType[] tabTypes = new TabType[]
    {
        TabType.Tab1,
        TabType.Tab2
    };

    private static string[] SceneNames = new string[]
    {
         "NROL",
         "NROL1"
    };
    public static TabType tab;

    private static bool SceneLoad(string sceneName)
    {
        for (int i = 0; i < SceneManager.sceneCount; i++)
        {
            if (SceneManager.GetSceneAt(i).name == sceneName)
            {
                return true;
            }
        }
        return false;
    }

    public static void ChangeTab(int index)
    {
        tab = tabTypes[index];
        if (!SceneLoad(SceneNames[index]))
        {
            SceneManager.LoadScene(SceneNames[index], LoadSceneMode.Additive);
        }
    }
}
