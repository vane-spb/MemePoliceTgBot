package io.github.vanespb.meme_police_bot.objects;

import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private String author;
    private String text;
    @Builder.Default
    private List<String> media = new ArrayList<>();
    @Builder.Default
    private List<File> mediaFiles = new ArrayList<>();
    private MessageDto reply;
    private PostDto repost;


    public void addMedia(String s) {
        media.add(s);
    }

    public void addFile(File s) {
        mediaFiles.add(s);
    }

    public String getFullText() {
        String baseText = StringUtils.isNotEmpty(text) ? String.format("%s:%n%s%n", author, text) :
                String.format("%s:%n", author);
        String replyText = this.reply != null ? String.format("forwarded from %s", this.reply.getFullText()) : "";
        String repostText = this.repost != null ? String.format("%n post %s%n", repost.getFullText()) : "";
        return baseText + replyText + repostText;
    }

    public List<File> getAllMediaFiles() {
        ArrayList<File> fileArrayList = new ArrayList<>(mediaFiles);
        if (reply != null && reply.mediaFiles != null)
            fileArrayList.addAll(reply.mediaFiles);
        if (repost != null && repost.getMediaFiles() != null)
            fileArrayList.addAll(repost.getMediaFiles());
        return fileArrayList;
    }

}
