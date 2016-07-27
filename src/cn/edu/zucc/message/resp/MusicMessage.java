package cn.edu.zucc.message.resp;

/**
 * Created by vito on 2016/7/25.
 */
public class MusicMessage extends BaseMessage {
    // 音乐
    private Music Music;

    public Music getMusic() {
        return Music;
    }

    public void setMusic(Music music) {
        Music = music;
    }
}
