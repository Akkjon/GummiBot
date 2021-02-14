package de.akkjon.pr.mbrm;

import net.dv8tion.jda.api.entities.Guild;
import org.apache.commons.text.StringEscapeUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Storage {

    public static String jarFolder;

    static {
        try {
            jarFolder = (new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            jarFolder = StringEscapeUtils.unescapeHtml4(new File(ClassLoader.getSystemClassLoader().getResource(".").getPath()).toString());
        }
    }

    public static final String rootFolder = jarFolder + File.separator + "servers" + File.separator;

    public static String getJarName() {
        String jarName = System.getProperty("java.class.path");
        if (jarName.contains(File.separator)) jarName = jarName.substring(jarName.lastIndexOf(File.separator) + 1);
        String path = Storage.jarFolder + File.separator + jarName;
        if (!path.endsWith(".jar")) {
            return null;
        }
        return path;
    }

    public static Long[] getServers() {
        List<Guild> guilds = Main.jda.getGuilds();
        Long[] out = new Long[guilds.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = guilds.get(i).getIdLong();
        }
        return out;
    }

    public static Long[] getChannels(long serverId) {
        String fileContent;
        try {
            fileContent = getFileContent(rootFolder + serverId + File.separator + "cleanup.txt", "[]");
        } catch (IOException e) {
            System.err.println("Error: cannot get channels for Server " + serverId);
            e.printStackTrace();
            return new Long[0];
        }
        try {
            Long[] arrLong = arrayFromString(fileContent);
            return arrLong;
        } catch (Exception e) {
            System.err.println("Error: cannot parse channels for Server " + serverId);
            e.printStackTrace();
            return new Long[0];
        }
    }

    public static Long[] addChannel(long serverId, long channelId) throws IOException {
        List<Long> channels = new ArrayList<>(Arrays.asList(getChannels(serverId)));

        if (!channels.contains(channelId)) {
            channels.add(channelId);
            Long[] output = channels.toArray(new Long[0]);
            save(serverId, output);
            return output;
        }
        return null;
    }

    public static Long[] removeChannel(long serverId, long channelId) throws IOException {
        List<Long> channels = new ArrayList<>(Arrays.asList(getChannels(serverId)));
        if (channels.contains(channelId)) {
            channels.remove(channelId);
            Long[] output = channels.toArray(new Long[0]);
            save(serverId, output);
            return output;
        }
        return null;
    }

    private static void save(long serverId, Long[] channels) throws IOException {
        File file = new File(rootFolder + serverId + File.separator + "cleanup.txt");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        FileWriter writer = new FileWriter(file);
        writer.write(Arrays.toString(channels));
        writer.close();
    }

    public static String getFileContent(String path, String defautVal) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String output = String.join("", reader.lines().toArray(String[]::new));
            reader.close();
            return output;
        }
        return defautVal;
    }

    public static List<String> getStringArrayFromFile(String input) {
        List<String> arrInput = Arrays.asList(input.replace("[", "").replace("]", "").split(", "));
        arrInput = arrInput.stream().filter(e -> e.trim().length() > 0).collect(Collectors.toList());
        return arrInput;
    }

    public static Long[] arrayFromString(String input) throws NumberFormatException {
        List<String> arrInput = getStringArrayFromFile(input);
        if (arrInput.size() == 0) {
            return new Long[0];
        }
        Long[] output = new Long[arrInput.size()];
        for (int i = 0; i < arrInput.size(); i++) {
            long l = Long.parseLong(arrInput.get(i));
            output[i] = l;
        }
        return output;
    }

    public static String getInternalFile(String path) {
        InputStream is = Main.class.getResourceAsStream("/" + path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        List<String> lines = reader.lines().collect(Collectors.toList());
        String response = String.join("\n", lines);
        return response;
    }
}
