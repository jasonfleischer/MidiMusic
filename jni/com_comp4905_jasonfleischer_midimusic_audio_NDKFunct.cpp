#include "com_comp4905_jasonfleischer_midimusic_audio_NDKFunct.h"
#include <android/log.h>

#include <vector>   // For std::vector<>
#include <cstring>  // For std::strlen()
#include <cstdio>   // For std::fopen(), std::fwrite(), std::fclose()
#include <string>
#include <sstream>
#include <iostream>

using namespace std;

// javah -jni -classpath bin/classes/ -d jni/ com.comp4905.jasonfleischer.midimusic.audio.NDKFunct

// cd /Users/Fleischer/Documents/HCN/repos/MidiMusic
// ndk-build clean
// ndk-build
// ndk-build APP_ABI=all (if needed)

typedef unsigned char byte;

/* First define a custom wrapper over std::vector<byte>
 * so we can quickly push_back multiple bytes with a single call.
 */
class MIDIvec: public std::vector<byte>{
	public:
		// Methods for appending raw data into the vector:
		template<typename... Args>
		void AddBytes(byte data, Args...args){
			push_back(data);
			AddBytes(args...);
		}
		template<typename... Args>
		void AddBytes(const char* s, Args...args){
			insert(end(), s, s + std::strlen(s));
			AddBytes(args...);
		}
		void AddBytes() { }
};

/* Define a class which encodes MIDI events into a track */
class MIDItrack: public MIDIvec{
	protected:
		unsigned /*delay,*/ running_status;
	public:
		MIDItrack() : MIDIvec(),/* delay(0),*/ running_status(0){ }

		// Methods for indicating how much time elapses:
		//void AddDelay(unsigned amount) { delay += amount; }

		void AddVarLen(unsigned t){
			if(t >> 21) AddBytes(0x80 | ((t >> 21) & 0x7F));
			if(t >> 14) AddBytes(0x80 | ((t >> 14) & 0x7F));
			if(t >>  7) AddBytes(0x80 | ((t >>  7) & 0x7F));
			AddBytes(((t >> 0) & 0x7F));
		}

		void Flush(){
			AddVarLen(0);
			//delay = 0;
		}

		// Methods for appending events into the track:
		template<typename... Args>
		void AddEvent(byte data, Args...args){
			/* MIDI tracks have the following structure:
			 *
			 * { timestamp [metaevent ... ] event } ...
			 *
			 * Each event is prefixed with a timestamp,
			 * which is encoded in a variable-length format.
			 * The timestamp describes the amount of time that
			 * must be elapsed before this event can be handled.
			 *
			 * After the timestamp, comes the event data.
			 * The first byte of the event always has the high bit on,
			 * and the remaining bytes always have the high bit off.
			 *
			 * The first byte can however be omitted; in that case,
			 * it is assumed that the first byte is the same as in
			 * the previous command. This is called "running status".
			 * The event may furthermore beprefixed
			 * with a number of meta events.
			 */
		   Flush();
		   if(data != running_status)
		   	   AddBytes(running_status = data);
		   AddBytes(args...);
		}

		template<typename... Args>
		void AddMetaEvent(byte metatype, byte nbytes, Args...args){
			Flush();
			AddBytes(0xFF, metatype, nbytes, args...);
		}

		// Key-related parameters: channel number, note number, pressure,
		void KeyOn2(int ch, int n, int p, int d) {
			AddBytes(0xc2, 0x90|ch, n, p, d);
	    }
		void KeyOff2(int ch, int n, int p)   {
			AddBytes(0xc2, 0x80|ch, n, p);
		}

		void KeyOn(int ch, int n, int p, int d) { if(n>=0)AddEvent(0x90|ch, n, p, d); }
		void KeyOff(int ch, int n, int p)   { if(n>=0)AddEvent(0x80|ch, n, p); }
		void KeyTouch(int ch, int n, int p) { if(n>=0)AddEvent(0xA0|ch, n, p); }

		// Events with other types of parameters:
		void Control(int ch, int c, int v) { AddEvent(0xB0|ch, c, v); }
		void Patch(int ch, int patchno)    { AddEvent(0xC0|ch, patchno); } // change instruments
		void Wheel(int ch, unsigned value) { AddEvent(0xE0|ch, value&0x7F, (value>>7)&0x7F); }
};


/* Define a class that encapsulates all methods needed to craft a MIDI file. */
class MIDIfile: public MIDIvec{
	protected:
		vector<MIDItrack> tracks;
		unsigned tempo;
	public:
		MIDIfile(int t) : MIDIvec(), tracks() {
			tempo = t;
		}

		MIDItrack& operator[] (unsigned trackno) {
			if(trackno >= tracks.size()) {
				tracks.reserve(16);
				tracks.resize(trackno+1);
			}

			MIDItrack& result = tracks[trackno];
			if(result.empty()) {

				result.AddMetaEvent(0x51, 0x03,  tempo>>16, tempo>>8, tempo);// if tempo = 1000000 = 0x0F4240
				//result.AddMetaEvent(0x51, 0x03, 0x0F, 0x42, 0x40); // tempo event
				result.AddMetaEvent(0x59, 0x02, // key sig event
									0x00, // C
									0x00);
				result.AddMetaEvent(0x58, 0x04,
									0x04, // numerator
									0x02, // denominator (2==4, because it's a power of 2)
								    0x30, // ticks per click (not used)
								    0x08); // time sig event
			}
			return result;
		}

		void Finish() {
			clear();
			// header
			AddBytes( "MThd", 0x00,0x00,0x00,0x06, // Mthd, chunk size 6
				0x00,0x00, // single-track format
				0x00,0x01, // one track
				0x00,0x10,
				"MTrk"); // 0x4d, 0x54, 0x72, 0x6B

			MIDItrack track = tracks[0];
			AddBytes(track.size() >> 24,
								track.size() >> 16,
								track.size() >> 8,
								track.size() >> 0);
			insert(end(), track.begin(), track.end());
			AddBytes(0x01, 0xFF, 0x2F, 0x00); // footer
		}
};

/*
 * Class:     com_comp4905_jasonfleischer_midimusic_audio_NDKFunct
 * Method:    writeSingleNoteFile
 * Signature: (IIIILjava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_comp4905_jasonfleischer_midimusic_audio_NDKFunct_writeSingleNoteFile
  (JNIEnv *env, jclass clazz, jint midiValue, jint instrument, jint velocity, jint noteDuration, jstring fileName, jint tempo){

	MIDIfile file(tempo);
	int channel = 0x00;
	const char *nativeString = env->GetStringUTFChars(fileName, JNI_FALSE);

	file[0].Patch(channel, instrument);
	file[0].KeyOn(channel, midiValue, velocity, noteDuration);
	file[0].KeyOff(channel, midiValue, 0x00);
	file.Finish();

	FILE* fp = fopen(nativeString, "wb");
	fwrite(&file.at(0), 1, file.size(), fp);
	fclose(fp);

	env->ReleaseStringUTFChars(fileName, nativeString);
	//__android_log_print(ANDROID_LOG_DEBUG, "MidiMusic NDK", "Done: createSingleNote %d ", midiValue);
}

/*
 * Class:     com_comp4905_jasonfleischer_midimusic_audio_NDKFunct
 * Method:    writeChordFile
 * Signature: (IIIILjava/lang/String;I)V
 */

JNIEXPORT void JNICALL Java_com_comp4905_jasonfleischer_midimusic_audio_NDKFunct_writeChordFile
  (JNIEnv *env, jclass clazz, jint midiValue, jint instrument, jint velocity, jint noteDuration, jstring fileName, jint tempo, jintArray intervals){

	__android_log_print(ANDROID_LOG_DEBUG, "MidiMusic NDK", "Done: createChord %d ", midiValue);
	MIDIfile file(tempo);
	int channel = 0x00;
	const jsize length = env->GetArrayLength(intervals);
	jint* ints = env->GetIntArrayElements(intervals, 0);
	const char *nativeString = env->GetStringUTFChars(fileName, JNI_FALSE);

	file[0].Patch(channel, instrument);
	file[0].KeyOn(channel, midiValue, velocity, 0x00);

	for(int i=0; i<length;i++){
		__android_log_print(ANDROID_LOG_DEBUG, "MidiMusic NDK", " + %d ", midiValue+ints[i]);
		if(i!=length-1)
			file[0].KeyOn(channel, midiValue+ints[i], velocity, 0x00);
		else
			file[0].KeyOn(channel, midiValue+ints[i], velocity, noteDuration);
	}
	file[0].KeyOff(channel, midiValue, 0x00);
	for(int i=0; i<length;i++){
		__android_log_print(ANDROID_LOG_DEBUG, "MidiMusic NDK", "  ++ %d ", midiValue+ints[i]);
		file[0].KeyOff(channel, midiValue+ints[i], 0x00);
	}
	file.Finish();

	FILE* fp = fopen(nativeString, "wb");
	fwrite(&file.at(0), 1, file.size(), fp);
	fclose(fp);

	env->ReleaseIntArrayElements(intervals, ints,  JNI_ABORT);
	env->ReleaseStringUTFChars(fileName, nativeString);
	__android_log_print(ANDROID_LOG_DEBUG, "MidiMusic NDK", "Done: createChord %d ", midiValue);
}

/*
 * Class:     com_comp4905_jasonfleischer_midimusic_audio_NDKFunct
 * Method:    writeSequenceFile
 * Signature: (IIILjava/lang/String;I[I)V
 */
JNIEXPORT void JNICALL Java_com_comp4905_jasonfleischer_midimusic_audio_NDKFunct_writeSequenceFile
  (JNIEnv *env, jclass clazz, jint midiValue, jint instrument, jint velocity, jstring fileName, jint tempo, jintArray sequence){

	MIDIfile file(tempo);
	int channel = 0;
	const char *nativeString = env->GetStringUTFChars(fileName, JNI_FALSE);

	file[0].Patch(channel, instrument);

	//TODO

	FILE* fp = std::fopen(nativeString, "wb");
	std::fwrite(&file.at(0), 1, file.size(), fp);
	std::fclose(fp);

	env->ReleaseStringUTFChars(fileName, nativeString);
	__android_log_print(ANDROID_LOG_DEBUG, "MidiMusic NDK", "Done: createSequence %d ", midiValue);
}


