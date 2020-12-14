Auction Project APIs


header "auth" : "Bearer 'Token'"


************************************************************************************************************************

-/auctions/category   GET   : get categories

************************************************************************************************************************

-/auctions/find/{id}    GET : get an auction
************************************************************************************************************************

-/auctions/add/picture/{id}   POST : set a picture for an auction
            @PathVariable int id, @RequestBody MultipartFile[] images

************************************************************************************************************************

-/auctions/add  POST : add a new Auction

        {
            title,description,basePrice,date,categoryId,maxNumber
        }


        430 : if title == null
        451 : if title.length > 50
        432 : if base_price == null
        452 : if description > 1000
        436 : if category doesn't exist
        438 : if date == null
        437 : if date is too soon (less than half an hour)
        434 : if max_number  < 2
        435 : if max_number > 15
        453 : if size of image is too high (>300MB)

        response => Resource<addAuctionDomain> : addAuctionDomain{
                   title,description,basePrice,date,categoryId,maxNumber
        }


************************************************************************************************************************

-auctions/addBookmark POST:
    @Param ("auctionId") Integer

    out : Resource<AuctionDomain>
    454 if auctionId = null
    455 if no auction found by Id
    456 if server DB contains null auction
************************************************************************************************************************
-auctions/bid


************************************************************************************************************************
************************************************************************************************************************

-/users/login : login  POST
    {
        email : String
        password : String
    }

************************************************************************************************************************

-/users/signup  POST : sign up
        {
            name,
            email,
            password
        }

        439 : if email = null
        441 : if password < 6
        442 : if password > 100
        440 : if name = null
        443 : if email isn't valid
        454 : if email is duplicated

        response => Resource<User> : User {
                name,email,picture,bookmarkes
        }

************************************************************************************************************************

-/users/edit  POST : edit name and email
              {
                name,
                email
              }

************************************************************************************************************************

-/users/edit/picture  POST : set a picture for user
             @RequestPart MultipartFile picture


************************************************************************************************************************

-/users/edit/password  POST : change password
             @RequestParam("oPassword") String oPassword
             @RequestParam("nPassword") String nPassword

              out : Resource<{name,email}>
                 441 if (nPassword | oPassword).length < 6
                 442 if (nPassword | oPassword).length > 100
                 499 if token expired or authentication failed (login required).

************************************************************************************************************************

-/users/me  GET : get details of current user
                  name,email,id,picture

************************************************************************************************************************
************************************************************************************************************************

-/home/search/{category} POST : search by title
        @RequestParam("title") String title, @PathVariable int category, @RequestParam("page") int page, @RequestParam("size") int size,
        if category was 0 it search with all categories :)

************************************************************************************************************************

-/home/hottest  GET : to receive sorted auctions (number of pages start with zero)
                             @RequestParam("page") int page, @RequestParam("size") int size

************************************************************************************************************************
************************************************************************************************************************

#// Password Recovery

-/forgot POST:
    @Param ("email") : string

    out : Resource<User>
    407 if not found email address

-/reset GET:
     @Param ("token") String

     out : Resource<User>
     448 if reset link is invalid

-/reset POST:
    @Param ("token") String
    @Param ("password") String

    out : Resource<User>
    449 if token not found
    450 if request hasn't been recorded
