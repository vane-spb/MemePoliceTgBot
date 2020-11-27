package io.github.vanespb.meme_police_bot.objects;

import lombok.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {
    private String publicName;
    private String text;
    private List<String> media = new ArrayList<>();
    private List<File> mediaFiles = new ArrayList<>();

    public void addMedia(String s) {
        media.add(s);
    }

    public void addFile(File s) {
        mediaFiles.add(s);
    }

    public String getFullText() {
        return String.format("%s: %n%s", publicName, text);
    }
}
