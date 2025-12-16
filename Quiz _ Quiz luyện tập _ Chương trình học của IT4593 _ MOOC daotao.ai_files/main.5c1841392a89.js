$(document).ready(function () {
  // Slider
  $('.main-slide').slick({
    slidesToShow: 1,
    slidesToScroll: 1,
    infinite: true,
    dots: true,
    prevArrow: '<span class="material-icons-outlined prev">chevron_left</span>',
    nextArrow:
      '<span class="material-icons-outlined next">chevron_right</span>',
    responsive: [
      {
        breakpoint: 1200,
        settings: {
          arrows: false,
        },
      },
    ],
  });

  // Set direction card
  var width = $(window).width();

  if (width > 992) {
    $('.js-card:nth-child(4n)').addClass('last');
  }
  if (width < 992 && width >= 768) {
    $('.js-card:nth-child(3n)').addClass('last');
  }
  if (width < 768) {
    $('.js-card:nth-child(2n)').addClass('last');
  }

  // Select language
  $('.js-language').on('click', '.dropdown-item', function (e) {
    e.preventDefault();
    let getLanguage = $(this).find('.dropdown-item__inner').clone();
    $('.js-language').find('.language-inner').html(getLanguage);
    $('.js-language').find('.dropdown-item').removeClass('choose');
    $(this).addClass('choose');
  });

  // Dropdown select
  $('.js-select').on('click', '.dropdown-item-option', function (e) {
    e.preventDefault();
    let value = $(this).find('.dropdown-item__inner').text();
    $(this).parents('.js-select').find('.select-inner').text(value);
    $(this)
      .parents('.js-select')
      .find('.dropdown-item-option')
      .removeClass('choose');
    $(this).addClass('choose');
  });

  // Set margin body
  var menuTopHeight = $('.menu-top').outerHeight();
  var headerHeight = $('.header').outerHeight();
  // $('.content-wrapper').css('margin-top', headerHeight);

  var resizeTimer;

  $(window).on('resize', function () {
    clearTimeout(resizeTimer);
    resizeTimer = setTimeout(function () {
      headerHeight = $('.header').outerHeight();
      $('.content-wrapper').css('margin-top', headerHeight);
    }, 250);
  });

  // Field comment
  $('.js-field-comment input').on('input', function () {
    let value = $(this).val();
    if (value.length > 0) {
      $(this)
        .parents('.js-field-comment')
        .find('.button')
        .removeAttr('disabled');
    } else {
      $(this)
        .parents('.js-field-comment')
        .find('.button')
        .attr('disabled', 'disabled');
    }
  });

  $('#close-course-detail').click(function () {
    $('.study-program-collapse .collapse').collapse('hide');
  });

  // Scroll sidebar
  function scrollSidebar(scrollTopHeight, typeScroll) {
    if (scrollTopHeight <= menuTopHeight) {
      switch (typeScroll) {
        case 'scrollUp':
          $('.sidebar').css(
            'top',
            `${headerHeight + (menuTopHeight - scrollTopHeight)}px`
          );
          break;
        default:
          $('.sidebar').css(
            'top',
            `${headerHeight + menuTopHeight - scrollTopHeight}px`
          );
          break;
      }
    } else {
      $('.sidebar').css('top', headerHeight);
    }
  }

  if ($('.sidebar').length) {
    let lastScrollTop = 0;
    $(window).scroll(function () {
      const scrollTopHeight = $(this).scrollTop();

      if (scrollTopHeight > lastScrollTop) {
        scrollSidebar(scrollTopHeight, 'scrollDown');
      } else {
        scrollSidebar(scrollTopHeight, 'scrollUp');
      }

      lastScrollTop = scrollTopHeight;
    });
  }
});

$(window).on('load', function () {
  $('.slide .container').removeClass('d-none');
});
