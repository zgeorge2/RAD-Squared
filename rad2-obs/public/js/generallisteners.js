/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

//Listener for all the timer button

$('.timer-button').unbind().click(function() {
	clearInterval(updateGraphIntervalId);
	document.close();
    dataInterval = Number($(this).attr('value'));
    console.log($(this).attr('value'));
    $('.timer-button').removeClass('btn-outline-primary');
    $('.timer-button').addClass('btn-outline-secondary');
    $(this).removeClass('btn-outline-secondary');
    $(this).addClass('btn-outline-primary');
    updateGraphAfterEveryTick();
    updateGraphIntervalId = setInterval(updateGraphAfterEveryTick, tickTime);
});

$(".edge-toggle-button").unbind().click(function() {
	isHidden[$(this).attr('value')] = !isHidden[$(this).attr('value')];
	updateEdgeWithHiddenTag($(this).attr('value'));
	$(this).toggleClass('btn-outline-success');
	$(this).toggleClass('btn-outline-danger');
});

$("#traffic-width").on("input change", function() {
    widthThreshold = $(this).val();
    console.log(widthThreshold)
});