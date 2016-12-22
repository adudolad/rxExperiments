package ch04.book;

import rx.Observable;

public interface BookService {
    Observable<Book> bestSeller();

    Observable<Book> recommendation(String customer);
}
