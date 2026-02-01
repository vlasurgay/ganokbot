package vlsurhai.ganokbot.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vlsurhai.ganokbot.common.model.jpa.ChatPermissions;

@Mapper(componentModel = "spring")
public interface ObjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chatId", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isFrozen", ignore = true)
    @Mapping(source = "source.canSendMessages", target = "target.canSendMessages")
    @Mapping(source = "source.canSendAudios", target = "target.canSendAudios")
    @Mapping(source = "source.canSendDocuments", target = "target.canSendDocuments")
    @Mapping(source = "source.canSendPhotos", target = "target.canSendPhotos")
    @Mapping(source = "source.canSendVideos", target = "target.canSendVideos")
    @Mapping(source = "source.canSendVideoNotes", target = "target.canSendVideoNotes")
    @Mapping(source = "source.canSendVoiceNotes", target = "target.canSendVoiceNotes")
    @Mapping(source = "source.canSendPolls", target = "target.canSendPolls")
    @Mapping(source = "source.canSendOtherMessages", target = "target.canSendOtherMessages")
    @Mapping(source = "source.canAddWebPagePreviews", target = "target.canAddWebPagePreviews")
    @Mapping(source = "source.canChangeInfo", target = "target.canChangeInfo")
    @Mapping(source = "source.canInviteUsers", target = "target.canInviteUsers")
    @Mapping(source = "source.canPinMessages", target = "target.canPinMessages")
    @Mapping(source = "source.canManageTopics", target = "target.canManageTopics")
    @Mapping(source = "source.canSendMediaMessages", target = "target.canSendMediaMessages")
    void mapChatPermissions(org.telegram.telegrambots.meta.api.objects.ChatPermissions source, @MappingTarget ChatPermissions target);

    @Mapping(source = "source.canSendMessages", target = "target.canSendMessages")
    @Mapping(source = "source.canSendAudios", target = "target.canSendAudios")
    @Mapping(source = "source.canSendDocuments", target = "target.canSendDocuments")
    @Mapping(source = "source.canSendPhotos", target = "target.canSendPhotos")
    @Mapping(source = "source.canSendVideos", target = "target.canSendVideos")
    @Mapping(source = "source.canSendVideoNotes", target = "target.canSendVideoNotes")
    @Mapping(source = "source.canSendVoiceNotes", target = "target.canSendVoiceNotes")
    @Mapping(source = "source.canSendPolls", target = "target.canSendPolls")
    @Mapping(source = "source.canSendOtherMessages", target = "target.canSendOtherMessages")
    @Mapping(source = "source.canAddWebPagePreviews", target = "target.canAddWebPagePreviews")
    @Mapping(source = "source.canChangeInfo", target = "target.canChangeInfo")
    @Mapping(source = "source.canInviteUsers", target = "target.canInviteUsers")
    @Mapping(source = "source.canPinMessages", target = "target.canPinMessages")
    @Mapping(source = "source.canManageTopics", target = "target.canManageTopics")
    @Mapping(source = "source.canSendMediaMessages", target = "target.canSendMediaMessages")
    void mapChatPermissions(ChatPermissions source, @MappingTarget org.telegram.telegrambots.meta.api.objects.ChatPermissions target);

    default org.telegram.telegrambots.meta.api.objects.ChatPermissions createFrozenPermissions() {
        return org.telegram.telegrambots.meta.api.objects.ChatPermissions.builder()
                .canSendMessages(false)
                .canSendAudios(false)
                .canSendDocuments(false)
                .canSendPhotos(false)
                .canSendVideos(false)
                .canSendVideoNotes(false)
                .canSendVoiceNotes(false)
                .canSendPolls(false)
                .canSendOtherMessages(false)
                .canAddWebPagePreviews(false)
                .canChangeInfo(false)
                .canInviteUsers(false)
                .canPinMessages(false)
                .canManageTopics(false)
                .canSendMediaMessages(false)
                .build();
    }
}
