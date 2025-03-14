package eu.sternenfighter.library.service;

import eu.sternenfighter.library.model.Book;
import eu.sternenfighter.library.repository.BookRepository;
import eu.sternenfighter.library.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * create a new book
     * @param income new book
     * @return new saved book
     * @throws IllegalArgumentException happens when category cannot be found
     */
    public Book saveBook(Book income) {
        if (categoryRepository.findById(income.getCategoryId()).isPresent()) {
            Book book = new Book();
            book.setAuthor(income.getAuthor());
            book.setYear(income.getYear());
            book.setTitle(income.getTitle());
            book.setPublisher(income.getPublisher());
            book.setCategoryId(income.getCategoryId());
            return bookRepository.save(book);
        }
        throw new IllegalArgumentException("Category not Found");
    }

    /**
     * update a given book
     * @param id book id
     * @param book book with new values
     * @return the updated book
     * @throws IllegalArgumentException book not found
     */
    public Book update(long id, Book book) {
        Optional<Book> optional = bookRepository.findById(id);
        if (optional.isPresent()) {
            book.setId(optional.get().getId());
            return bookRepository.save(book);
        }
        throw new IllegalArgumentException("Book not Found");
    }

    /**
     * delete a book by given id
     * @param id book id
     */
    public void delete(long id) {
        bookRepository.deleteById(id);
    }

    /**
     * find book by given id
     * @param id book id
     * @return book if exist
     */
    public Optional<Book> findById(long id) {
        return bookRepository.findById(id);
    }

    /**
     * find all books
     * @return list of all books
     */
    public List<Book> getAll() {
        return bookRepository.findAll();
    }
}
