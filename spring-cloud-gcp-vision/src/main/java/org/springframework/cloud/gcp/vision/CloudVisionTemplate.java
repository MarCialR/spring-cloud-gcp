/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gcp.vision;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Spring Template offering convenience methods for interacting with the Cloud Vision
 * APIs.
 *
 * @author Daniel Zou
 *
 * @since 1.1
 */
public class CloudVisionTemplate {

	private final ImageAnnotatorClient imageAnnotatorClient;

	public CloudVisionTemplate(ImageAnnotatorClient imageAnnotatorClient) {
		Assert.notNull(imageAnnotatorClient, "imageAnnotatorClient must not be null.");
		this.imageAnnotatorClient = imageAnnotatorClient;
	}

	/**
	 * Analyze an image and extract the features of the image specified by
	 * {@code featureTypes}.
	 * <p>A feature describes the kind of Cloud Vision analysis one wishes to perform on an
	 * image, such as text detection, image labelling, facial detection, etc. A full list of
	 * feature types can be found in {@link Feature.Type}.
	 * @param imageResource the image one wishes to analyze. The Cloud Vision APIs support
	 *     image formats described here: https://cloud.google.com/vision/docs/supported-files
	 * @param featureTypes the types of image analysis to perform on the image.
	 * @return the results of image analyses
	 */
	public AnnotateImageResponse analyzeImage(Resource imageResource, Feature.Type... featureTypes)
			throws IOException {

		ByteString imgBytes = ByteString.readFrom(imageResource.getInputStream());
		Image image = Image.newBuilder().setContent(imgBytes).build();

		List<Feature> featureList = Arrays.stream(featureTypes)
				.map(featureType -> Feature.newBuilder().setType(featureType).build())
				.collect(Collectors.toList());

		BatchAnnotateImagesRequest request = BatchAnnotateImagesRequest.newBuilder()
				.addRequests(
						AnnotateImageRequest.newBuilder()
								.addAllFeatures(featureList)
								.setImage(image))
				.build();

		BatchAnnotateImagesResponse batchResponse = this.imageAnnotatorClient.batchAnnotateImages(request);
		List<AnnotateImageResponse> annotateImageResponses = batchResponse.getResponsesList();

		return annotateImageResponses.get(0);
	}

}
