package com.megapro.invoicesync.dto;

import java.io.IOException;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.megapro.invoicesync.dto.response.ReadFileResponseDTO;
import com.megapro.invoicesync.model.FileModel;

@Mapper(componentModel="spring")
public interface FileMapper {
    @Mapping(target = "fileName", ignore = true)
    @Mapping(target = "fileType", ignore = true)
    @Mapping(target = "fileData", ignore = true)
    FileModel multipartfileToFileModel(MultipartFile file);

    @AfterMapping
    default void configureFile(@MappingTarget FileModel fileModel, MultipartFile file){
        fileModel.setFileName(StringUtils.cleanPath(file.getOriginalFilename()));
        fileModel.setFileType(file.getContentType());
        try {
            fileModel.setFileData(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Mapping(target = "fileUrl", ignore = true)
    ReadFileResponseDTO fileModelToReadFileResponseDTO (FileModel fileModel);

    @AfterMapping
    default void configureFileDTO(@MappingTarget ReadFileResponseDTO fileDTO, FileModel fileModel){
        fileDTO.setFileUrl("/approve-invoice/download-files/"+fileModel.getFileId());
    }
}
