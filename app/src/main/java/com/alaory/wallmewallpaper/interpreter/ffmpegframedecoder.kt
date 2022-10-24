package com.alaory.wallmewallpaper.interpreter


import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.Options
import wseemann.media.FFmpegMediaMetadataRetriever

class ffmpegframedecoder(private val source: ImageSource,
                         private val options: Options ) : Decoder{
    override suspend fun decode(): DecodeResult? {

        val ffmpegdecoder = FFmpegMediaMetadataRetriever();
        ffmpegdecoder.setDataSource(source.file().toFile().toString())
        val drawimg = ffmpegdecoder.frameAtTime.toDrawable(options.context.resources);

        return DecodeResult(
            drawimg,
            true
        )
    }


    class ffmpegfactory : Decoder.Factory{
        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            if (!isApplicable(result.mimeType)) return null
            val ffmpegframedc = ffmpegframedecoder(result.source,options);
            return ffmpegframedc;
        }
        private fun isApplicable(mimeType: String?): Boolean {
            return mimeType != null && mimeType.startsWith("video/")
        }
    }
}


