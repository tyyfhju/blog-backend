package com.zjy.blog.service;

import com.zjy.blog.entity.Photo;
import com.zjy.blog.entity.PhotoAlbum;
import com.zjy.blog.repository.PhotoAlbumRepository;
import com.zjy.blog.repository.PhotoRepository;
import com.zjy.blog.util.CosUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhotoAlbumService {

    @Autowired
    private PhotoAlbumRepository albumRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private CosUtil cosUtil;

    public List<PhotoAlbum> list() {
        return albumRepository.findAll();
    }

    public PhotoAlbum create(PhotoAlbum album) {
        return albumRepository.save(album);
    }

    public void delete(Long id) {
        List<Photo> photos = photoRepository.findByAlbumId(id);
        for (Photo photo : photos) {
            cosUtil.deleteByUrl(photo.getUrl());
            photoRepository.delete(photo);
        }
        albumRepository.deleteById(id);
    }
}