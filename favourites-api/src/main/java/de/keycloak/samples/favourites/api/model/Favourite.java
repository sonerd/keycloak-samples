package de.keycloak.samples.favourites.api.model;

public class Favourite {
    private String id;
    private Integer rating;
    private String comment;
    private String userName;

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getUserName() {
        return userName;
    }

    public static final class Builder {
        private String id;
        private Integer rating;
        private String comment;
        private String userName;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withRating(Integer rating) {
            this.rating = rating;
            return this;
        }

        public Builder withComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Favourite build() {
            Favourite favourite = new Favourite();
            favourite.comment = this.comment;
            favourite.id = this.id;
            favourite.rating = this.rating;
            favourite.userName = this.userName;
            return favourite;
        }
    }
}
