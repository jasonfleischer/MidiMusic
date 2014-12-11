package com.comp4905.jasonfleischer.midimusic.midi;

import java.io.ByteArrayOutputStream;

/**
 * {@link ByteArrayOutputStream} that can reset without memory leak.
 * @author K.Shoji
 */
public class ReusableByteArrayOutputStream extends ByteArrayOutputStream {
	private static final int DEFAULT_BUFFER_LIMIT = 1024;
	private final byte[] fixedSizeBuffer;

	/**
	 * Construct instance
	 * @param size
	 */
	private ReusableByteArrayOutputStream(int size) {
		super(size);
		fixedSizeBuffer = new byte[size];
		this.buf = fixedSizeBuffer;
	}

	/**
	 * Construct default instance, maximum buffer size is 1024 bytes.
	 */
	public ReusableByteArrayOutputStream() {
		this(DEFAULT_BUFFER_LIMIT);
	}

	/**
	 * (non-Javadoc)
	 * @see java.io.ByteArrayOutputStream#reset()
	 */
	@Override
	public synchronized void reset() {
		super.reset();

		if (this.buf.length > fixedSizeBuffer.length) { // reset buffer size when the buffer has been extended
			this.buf = fixedSizeBuffer;
		}
	}
}
