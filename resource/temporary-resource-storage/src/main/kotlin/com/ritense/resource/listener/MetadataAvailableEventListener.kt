package com.ritense.resource.listener

import com.ritense.temporaryresource.domain.ResourceStorageMetadata
import com.ritense.temporaryresource.domain.ResourceStorageMetadataId
import com.ritense.temporaryresource.domain.StorageMetadataKeys
import com.ritense.resource.event.ResourceStorageMetadataAvailableEvent
import com.ritense.temporaryresource.repository.ResourceStorageMetadataRepository
import org.springframework.context.event.EventListener

class MetadataAvailableEventListener(
    private val repository: ResourceStorageMetadataRepository
) {

    @EventListener(ResourceStorageMetadataAvailableEvent::class)
    fun storeResourceMetadata(event: ResourceStorageMetadataAvailableEvent) {
        if (event.documentId.isNotEmpty()) {
            repository.save(
                ResourceStorageMetadata(
                    ResourceStorageMetadataId(event.resourceId, StorageMetadataKeys.DOCUMENT_ID),
                    event.documentId
                )
            )
        }

        if (event.downloadUrl.isNotEmpty()) {
            repository.save(
                ResourceStorageMetadata(
                    ResourceStorageMetadataId(event.resourceId, StorageMetadataKeys.DOWNLOAD_URL),
                    event.downloadUrl
                )
            )
        }
    }
}