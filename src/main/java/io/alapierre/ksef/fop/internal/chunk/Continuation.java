package io.alapierre.ksef.fop.internal.chunk;

/**
 * Where a continuation chunk resumes its page sequence: the number and master of its first page and how many pages
 * of the same sequence came before it.
 */
final class Continuation {

    final int firstPageNumber;
    final String firstPageMaster;
    final int pagesBefore;

    Continuation(int firstPageNumber, String firstPageMaster, int pagesBefore) {
        this.firstPageNumber = firstPageNumber;
        this.firstPageMaster = firstPageMaster;
        this.pagesBefore = pagesBefore;
    }
}
