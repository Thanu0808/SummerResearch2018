package com.example.a96llegend.ar4ece.FSM;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.example.a96llegend.ar4ece.R;

import java.util.HashMap;
//Customized sound player for playing audio during animation
public class SoundPlayer {

    private static SoundPool mShortPlayer= null;
    private static HashMap mSounds = new HashMap();

    public SoundPlayer(Context pContext)
    {
        // setup Sound pool
        this.mShortPlayer = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        mSounds.put(R.raw.waiting, this.mShortPlayer.load(pContext, R.raw.waiting, 1));
    }

    public static int playShortResource(int piResource) {
        int iSoundId = (Integer) mSounds.get(piResource);
        return mShortPlayer.play(iSoundId, 0.99f, 0.99f, 0, -1, 1);
    }

    public static void stopSound(int streamID){
        mShortPlayer.stop(streamID);
    }

    // Cleanup
    public void release() {
        // Cleanup
        this.mShortPlayer.release();
        this.mShortPlayer = null;
    }
}
