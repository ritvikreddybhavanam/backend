package com.example.backend.service;

import com.example.backend.dto.MovieListItemRequest;
import com.example.backend.dto.MovieListRequest;
import com.example.backend.entity.MovieList;
import com.example.backend.entity.MovieListItem;
import com.example.backend.entity.User;
import com.example.backend.repository.MovieListItemRepository;
import com.example.backend.repository.MovieListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovieListService {

    private final MovieListRepository movieListRepository;
    private final MovieListItemRepository movieListItemRepository;

    public MovieListService(
            MovieListRepository movieListRepository,
            MovieListItemRepository movieListItemRepository
    ) {
        this.movieListRepository = movieListRepository;
        this.movieListItemRepository = movieListItemRepository;
    }

    @Transactional(readOnly = true)
    public List<MovieList> getMyLists(User user) {
        return movieListRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<MovieList> getPublicLists() {
        return movieListRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public MovieList getListById(Long id) {
        return movieListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("List not found"));
    }

    @Transactional
    public MovieList createList(MovieListRequest request, User user) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("List name is required");
        }

        MovieList list = new MovieList();
        list.setUser(user);
        list.setName(request.getName().trim());
        list.setDescription(request.getDescription());
        list.setGenre(request.getGenre());
        list.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);

        return movieListRepository.save(list);
    }

    @Transactional
    public MovieList updateList(Long listId, MovieListRequest request, User user) {
        MovieList list = getListById(listId);
        if (!list.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to update this list");
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            list.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            list.setDescription(request.getDescription());
        }
        if (request.getGenre() != null) {
            list.setGenre(request.getGenre());
        }
        if (request.getIsPublic() != null) {
            list.setIsPublic(request.getIsPublic());
        }

        return movieListRepository.save(list);
    }

    @Transactional
    public void deleteList(Long listId, User user) {
        MovieList list = getListById(listId);
        if (!list.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this list");
        }
        movieListRepository.delete(list);
    }

    @Transactional
    public MovieListItem addMovieToList(Long listId, MovieListItemRequest itemRequest, User user) {
        MovieList list = getListById(listId);
        if (!list.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to edit this list");
        }

        if (itemRequest.getMovieId() == null) {
            throw new RuntimeException("Movie ID is required");
        }

        if (movieListItemRepository.findByMovieListAndMovieId(list, itemRequest.getMovieId()).isPresent()) {
            throw new RuntimeException("Movie already exists in this list");
        }

        MovieListItem item = new MovieListItem();
        item.setMovieList(list);
        item.setMovieId(itemRequest.getMovieId());
        item.setTitle(itemRequest.getTitle() != null ? itemRequest.getTitle() : "Untitled Movie");
        item.setPoster(itemRequest.getPoster());
        item.setYear(itemRequest.getYear());
        item.setGenre(itemRequest.getGenre());
        item.setRating(itemRequest.getRating());
        item.setNote(itemRequest.getNote());
        item.setPosition(itemRequest.getPosition() != null ? itemRequest.getPosition() : list.getItems().size());

        return movieListItemRepository.save(item);
    }

    @Transactional
    public void removeMovieFromList(Long listId, Long movieId, User user) {
        MovieList list = getListById(listId);
        if (!list.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to edit this list");
        }

        movieListItemRepository.deleteByMovieListAndMovieId(list, movieId);
    }
}
